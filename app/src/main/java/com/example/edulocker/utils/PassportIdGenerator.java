package com.example.edulocker.utils;

import com.example.edulocker.models.School;

public class PassportIdGenerator {

    // Format: {STATE_CODE}-{DISTRICT_CODE}-{ORG_CODE}-{SEQ:03d}
    // Example: OD-KMD-PKS-001
    public static String generate(School school, int sequence) {
        String stateCode = school.getStateCode() != null && !school.getStateCode().isEmpty()
                ? school.getStateCode() : "IN";
        String districtCode = school.getDistrictCode() != null && !school.getDistrictCode().isEmpty()
                ? school.getDistrictCode() : "XX";
        return String.format("%s-%s-%s-%03d",
                stateCode,
                districtCode,
                school.getSchoolCode(),
                sequence);
    }

    // Auto-derive a 3-letter district code from district name (same logic as school code)
    public static String generateDistrictCode(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) return "XX";
        StringBuilder code = new StringBuilder();
        String[] words = districtName.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty() && Character.isLetter(word.charAt(0))) {
                code.append(Character.toUpperCase(word.charAt(0)));
                if (code.length() >= 3) break;
            }
        }
        while (code.length() < 3) code.append("X");
        return code.substring(0, 3);
    }

    // Generate a 6-letter school code from school name initials
    public static String generateSchoolCode(String schoolName) {
        StringBuilder code = new StringBuilder();
        String[] words = schoolName.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty() && Character.isLetter(word.charAt(0))) {
                // Skip common words
                String lower = word.toLowerCase();
                if (!lower.equals("of") && !lower.equals("the") && !lower.equals("and")
                        && !lower.equals("govt") && !lower.equals("government")
                        && !lower.equals("higher") && !lower.equals("secondary")) {
                    code.append(Character.toUpperCase(word.charAt(0)));
                }
                if (code.length() >= 3) break;
            }
        }
        // Pad to at least 3 chars
        while (code.length() < 3) code.append("X");
        return code.substring(0, Math.min(code.length(), 6));
    }

    // Generate a random 8-char password for school/teacher/student accounts
    public static String generatePassword(String name, String phone) {
        String base = name.replaceAll("\\s+", "").substring(0, Math.min(4, name.length()));
        String phoneSuffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : "0000";
        return base.substring(0, 1).toUpperCase() + base.substring(1).toLowerCase() + "@" + phoneSuffix;
    }

    // Unique school email — includes first 6 chars of schoolId UUID to avoid collisions
    public static String generateSchoolEmail(String schoolCode, String stateCode, String schoolId) {
        String suffix = schoolId.replace("-", "").substring(0, 6);
        return (schoolCode + "." + stateCode + "." + suffix + "@edulocker.in").toLowerCase();
    }

    // Unique teacher email — includes first 6 chars of teacherId UUID to avoid collisions
    public static String generateTeacherEmail(String schoolCode, String teacherName, String teacherId) {
        String namePart = teacherName.replaceAll("\\s+", ".").toLowerCase();
        String suffix = teacherId.replace("-", "").substring(0, 6);
        return (namePart + "." + schoolCode.toLowerCase() + "." + suffix + "@edulocker.in").toLowerCase();
    }

    public static String generateStudentEmail(String passportId) {
        return passportId.toLowerCase().replace("-", ".") + "@student.edulocker.in";
    }
}
