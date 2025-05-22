법정동 데이터 링크 : https://www.data.go.kr/data/15063424/fileData.do

## step 1 DB에 법정동 데이터 넣기
- insert_db.sh = 법정동 csv 파일을 로컬 mysql에 삽입하는 코드입니다.
csv파일을 sh 파일과 같은 경로에 두시고 실행하시면 됩니다.

## step 2 법정동 테이블에 위도, 경도 넣기
| 파이썬을 사용해본적이 없다면..
1. 파이썬을 설치하세요
2. 스크립트가 있는 경로에서 `pip install pymysql, requests`를 하세요.

- split_area_id.py = 데이터가 4만개 정도 되므로 만개 단위씩 실행하려고 했습니다. area 테이블의 area_id를 오름차순으로 정렬해 만개씩 자르는 스크립트입니다.
- kakao_map.py = 카카오 지도 API를 활용해 주소의 좌표를 얻어오는 스크립트입니다. REST_API 키를 입력해야하고, 카카오 디벨로퍼스 페이지에서 카카오맵 사용을 ON으로 바꿔야합니다. 위의 스크립트로 생성한 area_ids_batch_*.txt 파일의 이름을 손수 수정해서 사용하시면 됩니다.
  - 참고로 만개씩 나눠서 해도 오래걸립니다. 
  - 카카오맵 주소를 좌표로 변환하기의 일일 쿼터는 10만건입니다.


## step 3 인덱스 적용하기
- 빠른 거리 계산을 위해서 spatial 데이터 타입을 사용해야합니다.
- spatial 데이터 타입은 jpa로 자동 인덱스 적용이 안됩니다. 따라서 mysql에서 아래 쿼리를 직접 실행해주세요.
`ALTER TABLE area ADD SPATIAL INDEX(location);`


### etc
- area_id : 4148041022, 경기도 파주시 진서면 어용리 검색하면 안나와서 '어룡리'로 직.접 수정했습니다.
- 그 외 삭제되지 않았으나 카카오맵에서 검색되지 않는 지역들은 `failed_area_ids.txt` 에 기록되어있습니다.