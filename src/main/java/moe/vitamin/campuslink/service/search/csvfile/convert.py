import os
import re
import csv
from pypdf import PdfReader

# Keywords for classifying tokens
TIME_PATTERN = re.compile(r'([월화수목금토일])\((\d{2}:\d{2})[-~](\d{2}:\d{2})\)')

CLASSROOM_KWS = ["관", "실", "당", "MOOC", "MOO", "미지정", "가상", "운동장", "체육관", "강당", "단1", "단2", "로비"]
SUBFIELD_KWS = ["AI·SW", "진로", "취업", "의사소통", "글로벌", "문화", "기술", "예술", "역사", "철학", "체육", "소통", "균형", "인문", "사회", "과학", "소통", "독창", "의사"]
REMARKS_KWS = ["블렌디드", "러닝", "본교가상", "외국인", "온라인", "닝", "상", "교가"]

def classify_token(token):
    if TIME_PATTERN.search(token):
        return "time"
    
    if any(kw in token for kw in CLASSROOM_KWS):
        return "classroom"
        
    if any(kw in token for kw in SUBFIELD_KWS):
        return "sub_field"
        
    if any(kw in token for kw in REMARKS_KWS):
        return "remarks"
        
    return "professor"

def parse_pdf(pdf_path):
    reader = PdfReader(pdf_path)
    rows = []
    current_row = None
    
    for page in reader.pages:
        text = page.extract_text()
        if not text:
            continue
            
        lines = text.split('\n')
        for line in lines:
            line_str = line.strip()
            if not line_str:
                continue
                
            # Skip headers
            if "강의시간표" in line_str or line_str == "대학전체" or line_str == "구" or line_str == "분" or line_str == "학" or "년 수강학과 수강" in line_str or "번호 교과목명 학" in line_str or "점 시간 담당" in line_str or "교수 강의시간 강의실 소영역 비고" in line_str:
                continue
            if line_str.startswith("*") or "종합정보시스템" in line_str or "시간표" in line_str or "조회하실 수 있습니다" in line_str or "변경이 발생할 수 있으며" in line_str:
                continue
                
            tokens = line_str.split()
            
            # Find a 4-digit number that represents a course number (수강번호)
            course_num_idx = -1
            for idx, token in enumerate(tokens):
                if token.isdigit() and len(token) == 4:
                    course_num_idx = idx
                    break
                    
            if course_num_idx != -1:
                # Save previous row
                if current_row:
                    rows.append(current_row)
                    
                # New row!
                before = tokens[:course_num_idx]
                classification = ""
                grade = ""
                department = ""
                
                if len(before) == 3:
                    classification = before[0]
                    grade = before[1]
                    department = before[2]
                elif len(before) == 2:
                    if before[0].isdigit():
                        grade = before[0]
                    else:
                        classification = before[0]
                    department = before[1]
                elif len(before) == 1:
                    department = before[0]
                    
                course_number = tokens[course_num_idx]
                after = tokens[course_num_idx+1:]
                
                # Find hours token (e.g. 3-0-0)
                hours_idx = -1
                for idx, token in enumerate(after):
                    if '-' in token and len(token.split('-')) == 3:
                        hours_idx = idx
                        break
                        
                course_name = ""
                credits = ""
                hours = ""
                professor = ""
                class_time = ""
                classroom = ""
                sub_field = ""
                remarks = ""
                
                if hours_idx != -1:
                    credits_idx = hours_idx - 1
                    if credits_idx >= 0:
                        credits = after[credits_idx]
                        course_name_tokens = after[:credits_idx]
                    else:
                        course_name_tokens = after[:hours_idx]
                        
                    course_name = " ".join(course_name_tokens)
                    hours = after[hours_idx]
                    
                    # Process remaining tokens after hours
                    rem = after[hours_idx+1:]
                    for r_token in rem:
                        t_type = classify_token(r_token)
                        if t_type == "time":
                            if class_time:
                                class_time += " | " + r_token
                            else:
                                class_time = r_token
                        elif t_type == "classroom":
                            if classroom:
                                classroom += " " + r_token
                            else:
                                classroom = r_token
                        elif t_type == "sub_field":
                            if sub_field:
                                sub_field += " " + r_token
                            else:
                                sub_field = r_token
                        elif t_type == "remarks":
                            if remarks:
                                remarks += " " + r_token
                            else:
                                remarks = r_token
                        else:
                            if professor:
                                professor += " " + r_token
                            else:
                                professor = r_token
                else:
                    course_name = " ".join(after)
                    
                current_row = {
                    "classification": classification,
                    "grade": grade,
                    "department": department,
                    "course_number": course_number,
                    "course_name": course_name,
                    "credits": credits,
                    "hours": hours,
                    "professor": professor,
                    "class_time": class_time,
                    "classroom": classroom,
                    "sub_field": sub_field,
                    "remarks": remarks
                }
            else:
                # Continuation line
                if current_row:
                    for token in tokens:
                        t_type = classify_token(token)
                        if t_type == "time":
                            if current_row["class_time"]:
                                current_row["class_time"] += " | " + token
                            else:
                                current_row["class_time"] = token
                        elif t_type == "classroom":
                            if current_row["classroom"]:
                                current_row["classroom"] += " " + token
                            else:
                                current_row["classroom"] = token
                        elif t_type == "sub_field":
                            if current_row["sub_field"]:
                                current_row["sub_field"] += " " + token
                            else:
                                current_row["sub_field"] = token
                        elif t_type == "remarks":
                            if current_row["remarks"]:
                                current_row["remarks"] += " " + token
                            else:
                                current_row["remarks"] = token
                        else:
                            # Contextual fallback for continuation words
                            if current_row["professor"] and not current_row["classroom"]:
                                current_row["professor"] += " " + token
                            elif not current_row["professor"]:
                                current_row["professor"] = token
                            else:
                                if current_row["remarks"]:
                                    current_row["remarks"] += " " + token
                                else:
                                    current_row["remarks"] = token
                                    
    if current_row:
        rows.append(current_row)
        
    return rows

def convert_all():
    folder = r'C:\Users\kks43\Desktop\CampusLink\src\main\java\moe\vitamin\campuslink\service\search\csvfile'
    files = [f for f in os.listdir(folder) if f.lower().endswith('.pdf')]
    
    print(f"Found {len(files)} PDF files to convert.")
    
    for file in files:
        pdf_path = os.path.join(folder, file)
        csv_filename = file[:-4] + ".csv"
        csv_path = os.path.join(folder, csv_filename)
        
        print(f"Converting {file} -> {csv_filename}...")
        
        rows = parse_pdf(pdf_path)
        
        with open(csv_path, 'w', encoding='utf-8', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["구분", "학년", "수강학과", "수강번호", "교과목명", "학점", "시간", "담당교수", "강의시간", "강의실", "소영역", "비고"])
            
            for row in rows:
                writer.writerow([
                    row["classification"],
                    row["grade"],
                    row["department"],
                    row["course_number"],
                    row["course_name"],
                    row["credits"],
                    row["hours"],
                    row["professor"],
                    row["class_time"],
                    row["classroom"],
                    row["sub_field"],
                    row["remarks"]
                ])
                
        print(f"Successfully wrote {len(rows)} rows to {csv_filename}.")

if __name__ == "__main__":
    convert_all()
