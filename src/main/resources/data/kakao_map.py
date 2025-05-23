# source .venv/bin/activate

import pymysql
import requests
import time

KAKAO_REST_API_KEY = ''
DB_CONFIG = {
    'host': '',
    'user': '',
    'password': '',
    'db': 'picplz',
    'charset': 'utf8',
    'cursorclass': pymysql.cursors.DictCursor
}


def convert_address_to_coordinates(address):
    try:
        url = f'https://dapi.kakao.com/v2/local/search/address.json?query={address}'
        headers = {'Authorization': f'KakaoAK {KAKAO_REST_API_KEY}'}
        response = requests.get(url, headers=headers)

        if response.status_code == 200:
            documents = response.json().get("documents")
            if documents:
                x = documents[0]["address"]["x"]
                y = documents[0]["address"]["y"]
                return float(y), float(x), None
            else:
                return None, None, "NO_RESULT"
        else:
            return None, None, f"HTTP_{response.status_code}"
    except Exception as e:
        return None, None, f"EXCEPTION: {str(e)}"


def log_failed_area_id(area_id, name, reason, filepath='failed_area_ids.txt'):
    with open(filepath, 'a', encoding='utf-8') as f:
        f.write(f"{area_id} | {name} | {reason}\n")


def run_kakao_batch_from_file(filepath, start_from_area_id):
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cursor:
            with open(filepath, 'r') as f:
                lines = f.read().splitlines()

            start_found = False
            for line in lines:
                area_id = int(line.strip())

                if not start_found:
                    if area_id == start_from_area_id:
                        start_found = True
                    else:
                        continue

                cursor.execute("SELECT name, latitude, longitude, sido, sigungu, eupmyeondong, ri, deleted_date FROM area WHERE area_id = %s", (area_id,))
                result = cursor.fetchone()
                if not result:
                    print(f"[SKIP] area_id {area_id} not found in DB")
                    log_failed_area_id(area_id, "UNKNOWN", "NOT_FOUND")
                    continue
                if result['deleted_date'] is not None:
                    update_sql = "UPDATE area SET location = ST_SRID(ST_GeomFromText('POINT(0 0)'), 4326) WHERE area_id = %s"
                    cursor.execute(update_sql, (area_id,))
                    conn.commit()
                    print(f"[SKIP] area_id {area_id} deleted")
                    continue

                name = result['name']

                if result['latitude'] is not None and result['longitude'] is not None:
                    print(f"[SKIP] area_id {area_id} already has coordinates")
                    continue

                time.sleep(0.3)  # API 요청 간 딜레이
                lat, lng, reason = convert_address_to_coordinates(name)
                if reason is None:
                    point_text = f'POINT({lng} {lat})'
                    update_sql = """
                        UPDATE area
                        SET latitude = %s,
                            longitude = %s,
                            location = ST_SRID(ST_GeomFromText(%s), 4326)
                        WHERE area_id = %s
                    """
                    cursor.execute(update_sql, (lat, lng, point_text, area_id))
                    conn.commit()
                    print(f"[OK] Updated area_id {area_id} with ({lat}, {lng})")
                else:
                    # 법정동과 행정동의 차이로 결과를 얻어오지 못하는 경우가 있습니다.
                    # 이를 위해 시군구를 빼고 검색 하도록 했습니다.

                    parts = [result['sido'], result['eupmyeondong'], result['ri']]
                    name = ' '.join(part for part in parts if part)
                    lat, lng, reason = convert_address_to_coordinates(name)
                    if reason is None:
                        point_text = f'POINT({lng} {lat})'
                        update_sql = """
                            UPDATE area
                            SET latitude = %s,
                                longitude = %s,
                                location = ST_SRID(ST_GeomFromText(%s), 4326)
                            WHERE area_id = %s
                        """
                        cursor.execute(update_sql, (lat, lng, point_text, area_id))
                        conn.commit()
                        print(f"[OK] Updated area_id {area_id} with ({lat}, {lng})")
                    else:
                        # 그럼에도 실패하는 경우가 있습니다. 이 경우는 이유를 알 수 없습니다.
                        update_sql = "UPDATE area SET location = ST_SRID(ST_GeomFromText('POINT(0 0)'), 4326) WHERE area_id = %s"
                        cursor.execute(update_sql, (area_id,))
                        conn.commit()
                        print(f"[FAIL] area_id {area_id}, name {name}, reason: {reason}")
                        log_failed_area_id(area_id, name, reason)
                
    finally:
        conn.close()


if __name__ == "__main__":
    # 원하는 파일과 시작지점을 직접 입력할 수 있습니다.
    run_kakao_batch_from_file("area_ids_batch_1.txt", start_from_area_id=1100000000)