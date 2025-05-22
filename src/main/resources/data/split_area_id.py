import pymysql

DB_CONFIG = {
    'host': '',
    'user': '',
    'password': '',
    'db': 'picplz',
    'charset': 'utf8',
    'cursorclass': pymysql.cursors.DictCursor
}


def export_area_ids(batch_size=10000):
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cursor:
            cursor.execute("SELECT area_id FROM area ORDER BY area_id ASC")
            area_ids = [row['area_id'] for row in cursor.fetchall()]

            total_batches = (len(area_ids) + batch_size - 1) // batch_size

            for i in range(total_batches):
                batch = area_ids[i * batch_size:(i + 1) * batch_size]
                filename = f"area_ids_batch_{i + 1}.txt"
                with open(filename, 'w') as f:
                    for area_id in batch:
                        f.write(f"{area_id}\n")
                print(f"[OK] {filename} 저장 완료 ({len(batch)}개)")
    finally:
        conn.close()


if __name__ == "__main__":
    export_area_ids()