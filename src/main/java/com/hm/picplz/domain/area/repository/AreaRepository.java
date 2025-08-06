package com.hm.picplz.domain.area.repository;

import java.util.List;

import com.hm.picplz.domain.area.dto.AllAreaInfoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hm.picplz.domain.area.domain.Area;

public interface AreaRepository extends JpaRepository<Area, Long> {
	@Query(value =
		"SELECT * FROM area " +
			"WHERE deleted_date IS NULL " +
			"AND MBRContains(ST_GeomFromText(CONCAT(" +
			"'POLYGON((', ?1, ' ', ?2, ',', ?1, ' ', ?4, ',', ?3, ' ', ?4, ',', ?3, ' ', ?2, ',', ?1, ' ', ?2, '))'), 4326), location) " +
			"LIMIT 10",
		nativeQuery = true
	)
	List<Area> findAreaInMBR(double minLat, double minLng, double maxLat, double maxLng);

	@Query(
		value = "SELECT * FROM area " +
			"WHERE MATCH(name) AGAINST(CONCAT('*', :keyword, '*') IN BOOLEAN MODE) " +
			"AND deleted_date IS NULL " +
			"LIMIT 10",
		nativeQuery = true
	)
	List<Area> searchByKeyword(@Param("keyword") String keyword);

	@Query(value =
		"""
			SELECT
      		LEFT(sido, 2) AS sido,
      		IF(sido IN ('경기도', '제주특별자치도'), LEFT(sigungu, 3), sigungu) AS sigungu,
      		JSON_ARRAYAGG(eupmyeondong) AS neighborhoods
  			FROM area
			WHERE deleted_date IS NULL
    			AND eupmyeondong IS NOT NULL
    			AND sido IN ('서울특별시', '부산광역시', '인천광역시', '경기도', '제주특별자치도')
  			GROUP BY sido, sigungu;
		""", nativeQuery = true)
	List<AllAreaInfoProjection> findAllEupmyeondong();
}
