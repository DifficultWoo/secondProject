package com.secondproject.project.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.secondproject.project.entity.CategoryInfoEntity;
import com.secondproject.project.entity.ExpensesDetailEntity;
import com.secondproject.project.entity.MemberInfoEntity;
import com.secondproject.project.repository.CategoryInfoRepository;
import com.secondproject.project.repository.ExpensesDetailRepository;
import com.secondproject.project.repository.MemberInfoRepository;
import com.secondproject.project.vo.CategoryExpensesVO;
import com.secondproject.project.vo.DailyExpensesSearchVO;
import com.secondproject.project.vo.DailyExpensesVO;
import com.secondproject.project.vo.MonthExpensesResponseVO;
import com.secondproject.project.vo.PutExpensesVO;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ExpensesDetailService {
    private final MemberInfoRepository mRepo;
    private final ExpensesDetailRepository edRepo;
    private final CategoryInfoRepository cateRepo;

    public Map<String, Object> daily(DailyExpensesSearchVO search, DailyExpensesSearchVO pastSearch){
        Map<String, Object> map = new LinkedHashMap<>();
        List<DailyExpensesVO> list =edRepo.dailyExpenses(search);
        if(list.size()==0){
            MonthExpensesResponseVO month = new MonthExpensesResponseVO();
            map.put("status", false);
            map.put("message", "등록된 지출 내역이 존재하지 않습니다.");
            map.put("code", HttpStatus.NO_CONTENT);
            map.put("list", month);
            return map;
        }
        
        MonthExpensesResponseVO month = new MonthExpensesResponseVO();
        month.setting(list);
        month.changeRate(edRepo.totalSum(pastSearch));
        map.put("status", true);
        map.put("message", "조회했습니다.");
        map.put("code", HttpStatus.OK);
        map.put("list", month);
        
        return map;
    }
    public Map<String, Object> cateTotal(DailyExpensesSearchVO search){
        Map<String, Object> map = new LinkedHashMap<>();
        List<CategoryExpensesVO> list =edRepo.CategoryExpenses(search);
        if(list.size()==0){
            MonthExpensesResponseVO month = new MonthExpensesResponseVO();
            map.put("status", false);
            map.put("message", "등록된 지출 내역이 존재하지 않습니다.");
            map.put("code", HttpStatus.NO_CONTENT);
            map.put("list", month);
            return map;
        }
        int total = 0;
        for(CategoryExpensesVO cate : list){
            total += cate.getPrice();
        }
        for(CategoryExpensesVO cate : list){
            cate.countRate(total);
        }
        map.put("status", true);
        map.put("message", "조회했습니다.");
        map.put("code", HttpStatus.OK);
        map.put("cate", list);

        return map;
    }

    // 지출입력
    public Map<String, Object> putExpensesService(Long miSeq, PutExpensesVO data) {
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

        MemberInfoEntity member = mRepo.findById(miSeq).get();
        CategoryInfoEntity cate = cateRepo.findById(data.getCateSeq()).orElse(null);
        List<ExpensesDetailEntity> expenses = edRepo.findMemberAndCate(member, cate);
        if(cate != null && member != null) {
            ExpensesDetailEntity newExpenses = ExpensesDetailEntity.builder()
                .edMiSeq(member)
                .edAmount(data.getEdAmont())
                .edCateSeq(cateRepo.findById(data.getCateSeq()).get())
                .edTitle(data.getEdtitle())
                .edDate(data.getEdDate())
                .build();
            edRepo.save(newExpenses);
            resultMap.put("status", true);
            resultMap.put("message", "지출내역이 등록되었습니다.");
        }
        else {
            // 이부분이 아니라 값이 입력이 안되면 500에러가 뜸
            resultMap.put("status", false);
            resultMap.put("message", "지출내역이 등록되지 않았습니다.");
        }

        return resultMap;
    }

    // 지출 수정 (제목/ 카테고리/ 날짜/ 금액)
    // public void updateExpenses(Long miSeq, PlusMinusExpensesVO data) {
    //     Optional<ExpensesDetailEntity> findById = edRepo.findById(miSeq);
    //     if(findById.isPresent()) {
    //         ExpensesDetailEntity expenses = findById.get();
    //         CategoryInfoEntity cate = expenses.getEdCateSeq();

    //         expenses.PlusMinusExpensesVO(data.getCateSeq(), data.getEdAmount(), data.getEdtitle());
    //         return ;
    //     }
        
    // }
    

}
