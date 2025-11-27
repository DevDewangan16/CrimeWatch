# 🛡️ CrimeWatch

*A Community-Based Incident Reporting & Safety Alert Android Application (Kotlin + Jetpack Compose)*

CrimeWatch is an Android app that enables citizens to report incidents instantly, stay informed about nearby threats, and help build a safer community. Users can upload media evidence, add location details, and view verified incidents reported by others.
The system encourages community policing through transparency, upvotes, comments, and incident verification mechanisms.

---

## 📌 Project Overview

CrimeWatch solves the problem of **underreported crimes and delayed public awareness**.
crimes such as *theft, harassment, vandalism, and accidents* often go unreported due to:

* ❌ Fear of exposure
* ❌ Lack of trust
* ❌ No quick-access reporting platform

CrimeWatch provides a **digital reporting and alert system**, allowing:

✔ Citizens to report incidents with **location + media proof**
✔ Users to stay informed about **nearby threats**
✔ A **community safety network** backed by public validation
✔ Authorities (in future) to verify and act upon reports

---

## 🎯 Objectives


* Allow quick & easy crime/incident reporting.
* Enable **anonymous reporting**.
* Display incidents in a **feed + detailed view**.
* Provide **real-time alerts** for nearby reports.
* Support community validation via **upvotes / fake votes**.
* Add **comments** on incidents for discussions.
* Ensure privacy and prevent misuse with proper rules & access control.

---

## 🏗️ System Architecture

### 🧩 Tech Stack

| Component            | Technology                               |
| -------------------- | ---------------------------------------- |
| **Android App**      | Kotlin, Jetpack Compose, MVVM            |
| **Auth**             | Firebase Authentication (Google Sign-In) |
| **Database**         | Supabase (PostgreSQL + RLS)              |
| **Storage**          | Supabase Storage (Images / Videos)       |
| **Backend APIs**     | Supabase REST API                        |
| **State Management** | Kotlin Coroutines + StateFlow            |
| **Image Loading**    | Coil Compose                             |

---

## 📱 App Features

### 👤 **Authentication**

* Google Sign-In (with proper account picker)
* Secure Firebase Auth session handling

---

### 📝 **Report an Incident**

Users can report:

* Theft
* Accident
* Harassment
* Vandalism
* Municipal Issues
* Others

Each report contains:

* Category
* Description
* Location
* Media (Image/Video Upload)
* Timestamp
* Verification badge

The workflow matches the “Incident Reporting” part from the synopsis (page 2) .

---

### 🏠 **Home Feed**

Shows all incidents with:

* Image preview
* Category chip
* Description
* Location
* Time (human-friendly like “2 hours ago”)
* Verified badge
* “View Details” button

Auto-refresh or pull-to-refresh supported.

---

### 📝 **My Reports**

Shows **only the logged-in user's reports**, filtered using user_id.
Each row displays:

* Title (category)
* Time (formatted)
* “View Details”

---

### 📄 **Incident Detail Screen**

Includes:

* Full-screen image
* Category tag
* Verified badge
* Description
* Location
* Time
* **Community Verification**

  * Upvotes (“Verified”)
  * Fake votes
* **Comments Section**

  * Add a comment
  * See comments from all users

---

## 🗄️ Supabase Database Schema

### **Table: reports**

```sql
id uuid primary key,
user_id text,
category text,
description text,
location text,
media_url text,
verified boolean default false,
created_at timestamptz default now()
```

### **Table: comments**

```sql
id uuid primary key,
report_id uuid references reports(id),
user_id text,
comment text,
created_at timestamptz default now()
```

### **Table: votes**

```sql
id uuid primary key,
report_id uuid references reports(id),
user_id text,
vote_type text check (vote_type in ('upvote','fake')),
created_at timestamptz default now()
```

---

## 🔐 Supabase Storage

Bucket: **reports**

Stores uploaded media using PUT requests.
Public URL format:

```
https://<project>.supabase.co/storage/v1/object/public/reports/<fileName>
```

---

## 🔒 Row Level Security (RLS) Policies

To allow app users to insert/select safely:

### ✔ Reports

```sql
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "allow select" ON reports FOR SELECT USING (true);
CREATE POLICY "allow insert" ON reports FOR INSERT WITH CHECK (true);
```

### ✔ Comments

```sql
ALTER TABLE comments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "allow select comments" ON comments FOR SELECT USING (true);
CREATE POLICY "allow insert comments" ON comments FOR INSERT WITH CHECK (true);
```

### ✔ Votes

```sql
ALTER TABLE votes ENABLE ROW LEVEL SECURITY;

CREATE POLICY "allow select votes" ON votes FOR SELECT USING (true);
CREATE POLICY "allow insert votes" ON votes FOR INSERT WITH CHECK (true);
```

---

## 🧭 Navigation Flow

```
Splash → SignIn → Home  
               ↘ Report  
               ↘ My Reports  
Report → Success → Home  
Home → ReportDetails  
My Reports → ReportDetails
```

---

## 📂 Project Structure (Important Files)

```
/ui
   HomeScreen.kt
   ReportScreen.kt
   IncidentDetailScreen.kt
   MyReportScreen.kt
   CrimeApp.kt

/viewmodel
   CrimeViewModel.kt
   ReportsViewModel.kt

/data/supabase
   SupabaseApi.kt
   SupabaseRepository.kt
   SupabaseClient.kt
   DataModels.kt
```

---

## 🚀 Expected Outcomes

✔ Fully functional citizen reporting system
✔ Real-time community awareness
✔ Verified database of incidents
✔ Encourages community policing
✔ Useful for cities, colleges, residential societies

---

## 🧪 Testing

* Unit testing for ViewModels
* Supabase API endpoint testing
* Media upload reliability
* Real-time UI state testing with Jetpack Compose

---

### Software

* Android Studio (latest)
* Kotlin + Compose
* Supabase (DB + Auth + Storage)
* Firebase Auth
* GitHub (version control)

### Hardware

* Android 8.0+ device
* Developer system: i5+, 8GB RAM, 250GB storage

---
