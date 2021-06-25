/***********************************************************
 * @Description : 
 * @author      : 梁山广(Laing Shan Guang)
 * @date        : 2019-05-14 08:25
 * @email       : liangshanguang2@gmail.com
 ***********************************************************/
package com.huawei.l00379880.exam.repository;

import com.huawei.l00379880.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findByQuestionTypeId(Integer id);

    @Query("select q from Question q order by q.updateTime desc")
    List<Question> findAll();

    /**
     * 根据创建者id查找对应的问题
     * @param userId creator id
     * @return 问题列表
     */
    @Query("select q from Question q where q.questionCreatorId = ?1")
    List<Question> findByUserId(String userId);
}
