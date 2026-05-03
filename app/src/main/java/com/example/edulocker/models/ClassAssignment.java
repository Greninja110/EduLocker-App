package com.example.edulocker.models;

public class ClassAssignment {
    private String assignedClass; // e.g. "10"
    private String division;      // e.g. "A"
    private String subject;       // e.g. "Mathematics"

    public ClassAssignment() {}

    public ClassAssignment(String assignedClass, String division, String subject) {
        this.assignedClass = assignedClass;
        this.division = division;
        this.subject = subject;
    }

    public String getAssignedClass() { return assignedClass; }
    public void setAssignedClass(String assignedClass) { this.assignedClass = assignedClass; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    /**
     * Reads all class assignments for a teacher from a Firestore document snapshot.
     *
     * Combines two sources so that existing teachers (created before multi-class was added)
     * are not broken:
     *   1. Legacy top-level "assignedClass" / "division" / "subject" fields — always included first.
     *   2. "classAssignments" array — each entry added only if it differs from the legacy entry
     *      (deduplication by class + division).
     *
     * This means an old teacher who gets a second class added via arrayUnion will correctly
     * show BOTH their original class (from legacy fields) and the new one (from the array).
     */
    @SuppressWarnings("unchecked")
    public static java.util.List<ClassAssignment> fromDocSnapshot(
            com.google.firebase.firestore.DocumentSnapshot doc) {
        java.util.List<ClassAssignment> result = new java.util.ArrayList<>();

        // Step 1: legacy single-class fields (always present for every teacher)
        String legacyCls = doc.getString("assignedClass");
        if (legacyCls != null) {
            result.add(new ClassAssignment(
                    legacyCls,
                    doc.getString("division"),
                    doc.getString("subject")));
        }

        // Step 2: additional classes from the array — skip any that duplicate the legacy entry
        Object raw = doc.get("classAssignments");
        if (raw instanceof java.util.List) {
            for (Object item : (java.util.List<?>) raw) {
                if (!(item instanceof java.util.Map)) continue;
                java.util.Map<String, Object> m = (java.util.Map<String, Object>) item;
                String cls = (String) m.get("assignedClass");
                String div = (String) m.get("division");
                String sub = (String) m.get("subject");
                if (!alreadyIn(result, cls, div)) {
                    result.add(new ClassAssignment(cls, div, sub));
                }
            }
        }
        return result;
    }

    private static boolean alreadyIn(java.util.List<ClassAssignment> list, String cls, String div) {
        for (ClassAssignment ca : list) {
            if (eq(ca.getAssignedClass(), cls) && eq(ca.getDivision(), div)) return true;
        }
        return false;
    }

    private static boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    /** Display label: "10-A Mathematics" */
    public String label() {
        StringBuilder sb = new StringBuilder(assignedClass != null ? assignedClass : "");
        if (division != null && !division.isEmpty()) sb.append("-").append(division);
        if (subject != null && !subject.isEmpty()) sb.append(" ").append(subject);
        return sb.toString();
    }
}
