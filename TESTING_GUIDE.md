# BBJ Church Manager - Updated Testing Guide

## ✅ Completed Tasks

### 1. **Removed Sensitive Information**
- ✓ Removed admin credentials from login page
- ✓ Removed "Email: admin@bbj.com" from About modal
- ✓ Database reset with clean schema

### 2. **Enhanced Error Handling**
- ✓ Login error messages now display:
  - "missing" → "Please enter both username and password"
  - "invalid" → "Invalid username/email or password"
  - "server" → "Server error. Please try again later"

### 3. **Database Reset**
- ✓ Schema applied successfully
- ✓ Tables created: users, announcements, events, member_profiles, sermons
- ✓ Admin account: admin@bbj.com (profile_complete=true)

### 4. **New Member Registration System**
- ✓ Registration page with form validation
- ✓ Email generation from first name with duplicate handling
- ✓ Password strength indicator
- ✓ Automatic email preview as user types
- ✓ RegisterServlet with JSON response

---

## 🧪 Testing Workflows

### **Test 1: Admin Login Flow**
1. Go to http://localhost:8081/fresh_app/login.html
2. Enter: admin@bbj.com / admin123
3. Expected: Redirects to admin-dashboard.html

### **Test 2: Login Error Handling**
- Try: http://localhost:8081/fresh_app/login.html?error=server
- Expected: Red alert shows "Server error. Please try again later"

### **Test 3: Member Registration**
1. Go to http://localhost:8081/fresh_app/register.html
2. Fill form:
   - First Name: John
   - Last Name: Smith
   - Phone: (555) 123-4567
   - Address: 123 Main St
   - City: New York
   - State: NY
   - ZIP: 10001
   - Password: SecurePass123!
   - Confirm: SecurePass123!
3. Expected: Email shows as "john@bbj.com", account created, redirects to login

### **Test 4: Duplicate Email Handling**
1. Register first user: "Marie" → marie@bbj.com
2. Register second user: "Marie" → marie0@bbj.com
3. Register third user: "Marie" → marie1@bbj.com
4. Each should generate unique email

### **Test 5: Member Profile Completion Flow**
1. As new member login (if profile_complete=false)
2. Should redirect to member-profile.html
3. Complete profile form
4. Email generated automatically
5. Redirects to member-dashboard.html

### **Test 6: Learn More Modal**
1. On home page, click "Learn More"
2. Modal opens showing:
   - Features list (Announcements, Events, Sermons, etc.)
   - User roles explanation (no email displayed)
   - How it works
   - Mission statement

---

## 🎯 Current Application Status

**Deployment Info:**
- WAR File: 3.84 MB
- Last Modified: 2/21/2026 12:37:07 PM
- Tomcat Port: 8081
- Database: MySQL 9.6 on port 1532
- JDK: Java 17
- Framework: Bootstrap 5.3.0

**Available Pages:**
- /index.html (Home page with Sign In / Sign Up / Learn More)
- /login.html (Admin login)
- /register.html (Member registration) NEW
- /admin-dashboard.html (Admin control center)
- /member-dashboard.html (Member home)
- /member-profile.html (Profile completion)
- /announcements.html (View announcements)
- /events.html (View events)
- /create-announcement.html (Admin)
- /create-event.html (Admin)
- /members.html (Member directory)
- /sermons.html (Sermon listing)
- /upload-sermon.html (Admin sermon upload)

---

## 🚀 Planned Features

### **Phase 1 (In Progress)**
- [x] Member registration system
- [ ] Real file uploads for sermons (currently placeholder)
- [ ] Email verification system
- [ ] Password reset functionality

### **Phase 2 (Planned)**
- [ ] Admin member approval workflow
- [ ] Email notifications for events/announcements
- [ ] Member search and filtering
- [ ] Announcement scheduling
- [ ] Event RSVP system
- [ ] Sermon categories and tags

### **Phase 3 (Future)**
- [ ] SMS notifications
- [ ] Mobile app
- [ ] Group management
- [ ] Donation/Tithe system
- [ ] attendance tracking
- [ ] Prayer request system

---

## 📊 Database Configuration

**MySQL Connection:**
- Host: localhost
- Port: 1532
- User: root
- Password: fire@1532
- Database: church_manager

**Tables:**
1. **users** - Member/Admin accounts
   - Auto-generates email: firstname@bbj.com (with numeric index for duplicates)
   - Roles: admin, member
   - Password: SHA-256 hashed

2. **member_profiles** - Extended profile info
   - First name, last name, phone, address, city, state, postal code
   - Generated email unique per member

3. **announcements** - Church announcements
   - Title, content, created by, creation date

4. **events** - Church events
   - Title, description, date, location

5. **sermons** - Sermon recordings
   - Title, speaker, date, file URL/path

---

## 🔐 Security Features

- SHA-256 password hashing
- Session-based authentication
- Role-based access control (admin vs member)
- CSRF protection via form submission
- Email uniqueness validation
- Form input validation

---

## 📝 Known Issues & Notes

1. **Sermon uploads** - Currently placeholder system
   - Need to implement real file storage (local folder or cloud storage)
   - Suggested: Azure Blob Storage or AWS S3

2. **Email verification** - Not yet implemented
   - Recommended: Send verification link to member email
   - Confirm member before full account activation

3. **MySQL Service** - Manual startup required
   - Run: mysqld.exe with appropriate data directory
   - Or set up as Windows service for auto-start

4. **Tomcat Auto-Deploy** - Works but requires restart for some servlet changes
   - May need manual WAR redeployment in some cases

---

## 🎓 Next Steps

1. **Test Registration Flow**: Verify member registration works end-to-end
2. **Test Email Duplicates**: Confirm numeric indexing works (john0@, john1@, etc.)
3. **Implement Sermon Uploads**: Add real file upload capability
4. **Add Email Verification**: Send verification code to registered members
5. **Test Admin Dashboard**: Verify admin can create announcements and events
6. **Performance Testing**: Load test with multiple concurrent users

