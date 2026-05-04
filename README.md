# EduLocker

**A secure digital academic passport for students.**

EduLocker eliminates the pain of re-submitting the same documents for every scholarship or exam application. Verified institutions upload student documents once — students get a permanent digital passport and can apply to scholarships with a single tap, with all data pre-filled automatically.

---

## The Problem

Students must physically re-submit the same set of documents (marksheets, caste certificates, income certificates, etc.) for every scholarship, exam registration, or government scheme they apply to. This leads to:

- Lost or damaged original documents
- Repeated trips to institutions for attestation
- Delays in scholarship disbursement
- Poor adoption of digital government schemes

---

## The Solution

EduLocker gives every student a **Passport ID** — a permanent, verifiable digital identity. Schools upload documents once. Students carry their academic passport on their phone. Scholarship forms fill themselves.

---

## Features

### Student
- Digital academic passport with a unique Passport ID
- Virtual ID card (front + back) with QR code, shareable as image
- Auto-filled scholarship applications — all data + documents attached in one tap
- Document vault — view and download all uploaded documents by category
- Scholarship feed — only shows scholarships the student is actually eligible for
- News feed — education news with source filters
- KYC verification status tracking

### School Admin
- Register and manage student profiles
- Upload verified documents for students (marksheets, certificates, ID proofs)
- Manage teacher accounts
- Post notices for students and parents
- View detailed student and teacher profiles

### Teacher
- View assigned class students
- Access student documents
- Post and view notices
- Profile management

### Parent
- View their child's documents
- Track scholarship application status
- Read school notices
- Profile with child's academic summary

### Government
- Register and manage schools across districts
- Review and approve KYC verifications
- Manage scholarship listings
- View district-wise statistics and charts
- Manage government document types

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Platform | Android (minSdk 24, targetSdk 35) |
| Database | Firebase Firestore |
| File Storage | Firebase Storage |
| Authentication | Firebase Auth (Email/Password) |
| OTP / SMS | MSG91 REST API |
| Image Loading | Glide |
| QR Code | ZXing Android |
| Charts | MPAndroidChart |
| Animations | Lottie |
| HTTP Client | OkHttp |
| UI Components | Material Design 3, CircleImageView |

---

## Project Structure

```
app/src/main/java/com/example/edulocker/
│
├── activities/
│   ├── govt/                  # Government dashboard, school registration, KYC review
│   ├── school/                # School dashboard, add student/teacher, upload docs, notices
│   ├── teacher/               # Teacher dashboard
│   ├── parent/                # Parent dashboard
│   ├── SplashActivity         # Auth check + role-based routing
│   ├── LandingActivity        # Entry point for new users
│   ├── LoginActivity          # Shared login for all 5 roles
│   ├── MainActivity           # Student home (bottom nav)
│   ├── RegisterStep1/2        # New user OTP registration
│   ├── ScholarshipApplication # One-tap pre-filled scholarship form
│   ├── VirtualIdCardActivity  # Student ID card with QR + share
│   ├── KycVerificationActivity# KYC document review flow
│   └── AccountSwitcherActivity# Multi-account login switcher
│
├── fragments/
│   ├── HomeFragment           # Student home — eligible scholarships
│   ├── DocumentsFragment      # Student document vault
│   ├── SearchFragment         # Keyword search across student's docs
│   ├── NewsFragment           # Education news with source filters
│   ├── ProfileFragment        # Student profile → Virtual ID card
│   ├── govt/                  # Govt home, schools, notices, profile
│   ├── school/                # School home, students, notices, profile
│   ├── teacher/               # Teacher home, students, notices, profile
│   └── parent/                # Parent home, documents, notices, profile
│
├── repositories/
│   ├── AuthRepository         # Firebase Auth + user doc creation
│   ├── StudentRepository      # Student CRUD + Firestore queries
│   ├── DocumentRepository     # Upload to Firebase Storage + Firestore
│   ├── ScholarshipRepository  # Eligibility logic + application submission
│   ├── SchoolRepository       # School CRUD
│   ├── NoticeRepository       # Notice post/fetch
│   ├── NewsRepository         # News feed with caching
│   ├── DocumentTypeRepository # Govt-managed document type catalogue
│   └── VacancyRepository      # Teacher vacancy management
│
├── adapters/                  # RecyclerView adapters (13 total)
│
├── models/                    # Plain Java models (Student, School, Teacher,
│   │                          #   EduDocument, Scholarship, Notice, NewsItem…)
│
└── utils/
    ├── Constants              # All Firestore collection names + district codes
    ├── SessionManager         # SharedPreferences — login state, role, passportId
    ├── PassportIdGenerator    # Passport ID + auto credential generation
    ├── Msg91Helper            # OTP send/verify + SMS credentials delivery
    ├── VirtualIdCardGenerator # View → Bitmap export for ID card sharing
    ├── QRCodeHelper           # QR code generation (ZXing)
    ├── AccountManager         # Multi-account save/switch
    └── NewsCache              # In-memory news feed cache
```

---

## Firestore Data Model

```
/users/{uid}
    role, name, email, phone, schoolId

/schools/{schoolId}
    name, district, districtCode, schoolCode,
    loginEmail, studentCount, teacherCount

/students/{passportId}          ← passportId is the document ID
    userId, schoolId, parentUserId, name, dob,
    class, kycStatus, loginEmail, ...

/documents/{docId}
    passportId, type, title, fileUrl, verified, timestamp

/teachers/{teacherId}
    userId, schoolId, name, subject, assignedClass

/notices/{noticeId}
    schoolId, title, content, postedByUserId, timestamp

/scholarships/{id}
    title, eligibleCategory, eligibleClass,
    amount, deadline, issuedBy

/applications/{id}
    passportId, scholarshipId, status,
    attachedDocIds[], submittedAt

/news/{newsId}
    source, title, content, imageUrl, timestamp
```

---

## Student Passport ID Format

```
{STATE}-{DISTRICT_CODE}-P-{SCHOOL_CODE}-{SEQUENCE}

Example: ODIST-KMD-P-PKS-001
```

Generated by `PassportIdGenerator.generate(school, sequence)`. Used as the Firestore document ID for every student — all document and application queries key off this.

---

## User Roles & Login Flow

```
SplashActivity
  ├── Active Firebase session → getUserRole() → route to role dashboard
  └── No session → LandingActivity
        ├── Login → LoginActivity (shared across all roles)
        └── Register → RegisterStep1Activity (OTP) → RegisterStep2Activity
```

| Role | Dashboard | Created By |
|---|---|---|
| `government` | `GovtDashboardActivity` | Pre-seeded in Firebase |
| `school` | `SchoolDashboardActivity` | Government via RegisterSchoolActivity |
| `teacher` | `TeacherDashboardActivity` | School admin via AddTeacherActivity |
| `parent` | `ParentDashboardActivity` | Auto-created when student is added |
| `student` | `MainActivity` (bottom nav) | School admin via AddStudentActivity |

---

## Auto-Generated Credentials

No user ever chooses a password. All credentials are generated and delivered via SMS:

| Credential | Format Example |
|---|---|
| School email | `pks.kmd@edulocker.in` |
| Teacher email | `pks.priya@edulocker.in` |
| Student email | `ODIST-KMD-P-PKS-001@edulocker.in` |
| Password | `Priy@6658` (name + last 4 digits of phone) |

After account creation, credentials are shown in a dialog with a Copy button and sent via SMS.

---

## Setup & Build

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 11
- A Firebase project with the following enabled:
  - Authentication → Email/Password
  - Firestore Database
  - Storage
- MSG91 account (Sandbox mode works for testing)

### Steps

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd EduLocker
   ```

2. **Add Firebase config**

   Download `google-services.json` from your Firebase Console and place it at:
   ```
   app/google-services.json
   ```

3. **Add secrets**

   Create `secrets.properties` at the project root:
   ```properties
   MSG91_API_KEY=your_msg91_api_key
   MSG91_TEMPLATE_ID=your_template_id
   MSG91_SENDER_ID=EDULKR
   NEWSDATA_API_KEY=your_newsdata_api_key
   ```
   > For testing, you can leave MSG91 keys blank — the app will fall back gracefully. Use MSG91 Sandbox (fixed OTP: `123456`) to test OTP flows without DLT registration.

4. **Build and run**
   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Install directly on connected device
   ./gradlew installDebug

   # Clean build
   ./gradlew clean assembleDebug
   ```

---

## Codebase Size

| Layer | Lines of Code |
|---|---|
| Java — Activities | 5,000 |
| Java — Fragments | 3,241 |
| Java — Repositories | 1,290 |
| Java — Adapters | 1,061 |
| Java — Models | 709 |
| Java — Utils | 674 |
| **Total Java** | **11,975** |
| XML — Layouts | 7,634 |
| XML — Drawables / Values / Menus | 712 |
| **Total XML (UI)** | **8,346** |
| **Grand Total** | **~20,300** |

---

## Design System

- **Primary colour:** `#C2185B` (deep pink/magenta)
- **Gradient buttons:** `#E91E8C` → `#7B1FA2`
- **App background:** `#FFF0F5` (soft pink-cream)
- **ID card / profile background:** `#7B1530` (dark maroon)
- **Typography:** sans-serif-light for headings, standard for body
- Light mode only — night theme is force-disabled

---

## Known Prototype Limitations

- No partial rollback if `AddStudentActivity` fails mid-way (creates parent + student accounts in sequence)
- MSG91 requires DLT registration for production SMS delivery — Sandbox mode only for prototype
- News feed is manually seeded via `SeedDataActivity` — no live RSS integration
- No offline support — requires active internet for all Firestore operations
- Scholarship eligibility is based on category + class only (no income/marks filtering yet)

---

## Author

**Abhijeet Sahoo**
