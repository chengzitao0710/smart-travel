package com.smarttravel.scenic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttravel.scenic.entity.Scenic;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScenicMapper extends BaseMapper<Scenic> {

    @Override
    @Insert("INSERT INTO tb_scenic (name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours, description, tags, status) " +
            "VALUES (#{name}, #{typeId}, #{images}, #{area}, #{address}, #{x}, #{y}, " +
            "#{avgPrice}, #{sold}, #{comments}, #{score}, #{openHours}, #{description}, #{tags}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Scenic scenic);

    @Override
    @Update("UPDATE tb_scenic SET name=#{name}, type_id=#{typeId}, images=#{images}, area=#{area}, " +
            "address=#{address}, x=#{x}, y=#{y}, " +
            "avg_price=#{avgPrice}, sold=#{sold}, comments=#{comments}, score=#{score}, " +
            "open_hours=#{openHours}, description=#{description}, tags=#{tags}, status=#{status} " +
            "WHERE id=#{id}")
    int updateById(Scenic scenic);

    @Update("UPDATE tb_scenic SET sold = sold + 1 WHERE id = #{scenicId}")
    int incrementSold(Long scenicId);

    @Update("UPDATE tb_scenic SET comments = comments + 1 WHERE id = #{scenicId}")
    int incrementComments(Long scenicId);

}