#!/bin/bash

# ======================
# ✨ 설정 부분 (필수로 수정)
# ======================
USER=""                     # MySQL 사용자명
PASSWORD=""       # 비밀번호
DATABASE="picplz"       # DB 이름
TABLE="area"           # 테이블 이름
CSV_FILE="국토교통부_전국 법정동_20250415.csv"          # CSV 파일명
START_LINE=2                   # 시작 줄 번호 (1 = 헤더니까 2부터)


# ======================
# ✅ 실행 시작
# ======================
CURRENT_LINE=$((START_LINE - 1))

# 문자열 값이 비어있으면 NULL, 아니면 '값'으로 반환
sql_string_or_null() {
  local val="$1"
  if [[ -z "$val" ]]; then
    echo "NULL"
  else
    # 작은따옴표 이스케이프 처리
    val="${val//\'/\\\'}"
    echo "'$val'"
  fi
}

tail -n +$START_LINE "$CSV_FILE" | while IFS=',' read -r -a row
do
  ((CURRENT_LINE++))

  # 누락 필드 보정: 배열 길이 < 9일 경우 빈 값 추가
  for ((i=${#row[@]}; i<9; i++)); do
    row[i]=""
  done

  # 각 필드 매핑
  id="${row[0]}"
  sido="${row[1]}"
  sigungu="${row[2]}"
  eup="${row[3]}"
  ri="${row[4]}"
  order="${row[5]}"
  created="${row[6]}"
  deleted="${row[7]}"
  old_code="${row[8]}"

  # 🧼 CR 문자 제거 (숨겨진 원인 제거!)
  deleted=$(echo "$deleted" | tr -d '\r')
  old_code=$(echo "$old_code" | tr -d '\r')

  # 필수값 확인
  if [[ -z "$id" || -z "$sido" ]]; then
    echo "🚫 필수값 누락 (줄 $CURRENT_LINE), 건너뜀"
    continue
  fi

  # name 생성 (sido는 무조건, 이후 값이 있을 때만 추가)
  name="$sido"
  if [[ -n "$sigungu" ]]; then
    name+=" $sigungu"
  else
    echo
    # sigungu가 없으면 eup, ri도 무시 (sido만 name에)
    name="$sido"
  fi
  if [[ -n "$eup" ]]; then
    name+=" $eup"
  fi
  if [[ -n "$ri" ]]; then
    name+=" $ri"
  fi

  # 문자열/NULL 처리
  sigungu_sql=$(sql_string_or_null "$sigungu")
  eup_sql=$(sql_string_or_null "$eup")
  ri_sql=$(sql_string_or_null "$ri")
  name_sql=$(sql_string_or_null "$name")
  sido_sql=$(sql_string_or_null "$sido")

  # 숫자 및 날짜/NULL 처리
  [[ -z "$order" ]] && order_sql="NULL" || order_sql="$order"
  [[ -z "$created" ]] && created_sql="NULL" || created_sql="STR_TO_DATE('$created', '%Y-%m-%d')"
  [[ -z "$deleted" ]] && deleted_sql="NULL" || deleted_sql="STR_TO_DATE('$deleted', '%Y-%m-%d')"
  [[ -z "$old_code" ]] && old_code_sql="NULL" || old_code_sql="$old_code"

  # SQL 구문
  SQL="INSERT INTO $TABLE (
    id, sido, sigungu, eupmyeondong, ri, name,
    area_order, created_date, deleted_date, old_code
  ) VALUES (
    $id, $sido_sql, $sigungu_sql, $eup_sql, $ri_sql, $name_sql, $order_sql, $created_sql, $deleted_sql, $old_code_sql
  );"

  echo "$SQL"  # 필요 시 SQL 직접 확인

  # 쿼리 실행
  mysql -u "$USER" -p"$PASSWORD" "$DATABASE" -e "$SQL"

  if [ $? -ne 0 ]; then
    echo "❌ 실패! 줄 번호: $CURRENT_LINE"
    echo "🔁 다시 시작하려면: START_LINE=$CURRENT_LINE"
    exit 1
  fi

  echo "✅ 성공: 줄 $CURRENT_LINE"

done