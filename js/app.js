/**
 * Hostel Management System (HMS) - Master Application Controller (Full-Stack Backend Integrated)
 */

class Validator {
  static validateName(name) {
    if (!name || !name.trim()) return "Full name is required.";
    const regex = /^[a-zA-Z\s.]{2,50}$/;
    if (!regex.test(name.trim())) return "Full name must contain only letters, periods, and spaces (2 to 50 characters).";
    return null;
  }

  static validateEmail(email) {
    if (!email || !email.trim()) return "Email address is required.";
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!regex.test(email.trim())) return "Please enter a valid email address (e.g. user@domain.com).";
    return null;
  }

  static validatePhone(phone) {
    if (!phone || !phone.trim()) return "Phone number is required.";
    const digitsOnly = phone.trim().replace(/[\s-]/g, '');
    const regex = /^[0-9]{10}$/;
    if (!regex.test(digitsOnly)) return "Phone number must be a valid 10-digit number (e.g. 9876543210).";
    return null;
  }

  static validatePassword(password) {
    if (!password || !password.trim()) return "Password is required.";
    if (password.length < 6) return "Password must be at least 6 characters long.";
    return null;
  }

  static validateConfirmPassword(password, confirmPassword) {
    if (!confirmPassword) return "Confirm password is required.";
    if (password !== confirmPassword) return "Passwords do not match.";
    return null;
  }

  static validateRequired(value, fieldName) {
    if (!value || (typeof value === 'string' && !value.trim())) {
      return `${fieldName} is required.`;
    }
    return null;
  }

  static validateLeaveDates(startDateStr, endDateStr) {
    if (!startDateStr) return { field: 'start', message: "Start date is required." };
    if (!endDateStr) return { field: 'end', message: "End date is required." };

    const todayStr = new Date().toISOString().split('T')[0];
    if (startDateStr < todayStr) {
      return { field: 'start', message: "Start date cannot be in the past." };
    }
    if (endDateStr < startDateStr) {
      return { field: 'end', message: "End date cannot be earlier than start date." };
    }
    return null;
  }

  static validateComplaint(category, description) {
    if (!category) return { field: 'category', message: "Category is required." };
    if (!description || !description.trim()) return { field: 'description', message: "Issue description is required." };
    if (description.trim().length < 5) return { field: 'description', message: "Description must be at least 5 characters long." };
    if (description.trim().length > 500) return { field: 'description', message: "Description cannot exceed 500 characters." };
    return null;
  }

  static validateRoom(number, floor, capacity) {
    if (!number || !number.trim()) return { field: 'number', message: "Room number is required." };
    const numRegex = /^[A-Za-z0-9-]+$/;
    if (!numRegex.test(number.trim())) return { field: 'number', message: "Room number must contain letters, numbers, or hyphens." };

    if (floor === undefined || floor === null || isNaN(floor) || floor < 0) {
      return { field: 'floor', message: "Floor level must be 0 or higher." };
    }
    if (!capacity || isNaN(capacity) || capacity < 1 || capacity > 10) {
      return { field: 'capacity', message: "Capacity must be between 1 and 10." };
    }
    return null;
  }

  static validateRule(title, description) {
    if (!title || !title.trim()) return { field: 'title', message: "Rule title is required." };
    if (title.trim().length < 3) return { field: 'title', message: "Title must be at least 3 characters long." };

    if (!description || !description.trim()) return { field: 'description', message: "Rule description is required." };
    if (description.trim().length < 5) return { field: 'description', message: "Description must be at least 5 characters long." };
    return null;
  }
}

class AppController {
  constructor() {
    this.currentView = null;
    this.init();
  }

  clearFormErrors(formElement) {
    if (!formElement) return;
    const invalidInputs = formElement.querySelectorAll('.is-invalid');
    invalidInputs.forEach(el => el.classList.remove('is-invalid'));
    const errorElements = formElement.querySelectorAll('.invalid-feedback');
    errorElements.forEach(el => {
      el.innerText = '';
      el.style.display = 'none';
    });
  }

  showFieldError(inputElement, errorMsg) {
    if (!inputElement) return;
    inputElement.classList.add('is-invalid');
    
    let errDiv = inputElement.parentElement.querySelector('.invalid-feedback');
    if (!errDiv) {
      const fieldWrap = inputElement.closest('.input-field-wrap, .form-group');
      if (fieldWrap) {
        errDiv = fieldWrap.querySelector('.invalid-feedback');
      }
    }
    
    if (!errDiv) {
      errDiv = document.createElement('div');
      errDiv.className = 'invalid-feedback';
      const container = inputElement.closest('.input-field-wrap, .form-group') || inputElement.parentElement;
      container.appendChild(errDiv);
    }
    
    errDiv.innerText = errorMsg;
    errDiv.style.display = 'block';
  }

  async init() {
    this.applyTheme(store.getTheme());
    this.setupEventListeners();

    window.addEventListener('hms:unauthorized', () => {
      this.showToast('Your session has expired. Please sign in again.', 'danger');
      this.showHomeLayout();
    });

    // Refresh store from backend API if user is authenticated or public
    await store.refreshAllData();

    // Check if user is currently logged in
    const currentUser = store.getCurrentUser();
    if (currentUser) {
      this.showDashboardLayout();
      await this.switchRole(currentUser.role);
    } else {
      this.showHomeLayout();
    }
  }

  // Layout View Switchers (Split Login Landing vs Authenticated Dashboard)
  showHomeLayout() {
    const splitLanding = document.getElementById('login-landing-view');
    const dashWrapper = document.getElementById('dashboard-wrapper');
    if (splitLanding) splitLanding.style.display = 'grid';
    if (dashWrapper) dashWrapper.style.display = 'none';
  }

  showDashboardLayout() {
    const splitLanding = document.getElementById('login-landing-view');
    const dashWrapper = document.getElementById('dashboard-wrapper');
    if (splitLanding) splitLanding.style.display = 'none';
    if (dashWrapper) dashWrapper.style.display = 'flex';
  }

  setLoginTab(role) {
    document.querySelectorAll('.split-role-btn').forEach(btn => btn.classList.remove('active'));
    const activeBtn = document.getElementById(`tab-${role}`);
    if (activeBtn) activeBtn.classList.add('active');

    const roleInput = document.getElementById('login-tab-role');
    const prevRole = roleInput ? roleInput.value : '';
    if (roleInput) roleInput.value = role;

    const subToggle = document.getElementById('student-sub-toggle');
    if (subToggle) {
      subToggle.style.display = role === 'student' ? 'flex' : 'none';
    }

    // Default to signin mode
    this.toggleStudentAuthMode('signin');

    const userInput = document.getElementById('split-username');
    const passInput = document.getElementById('split-password');
    const hintText = document.getElementById('demo-credentials-text');

    if (prevRole && prevRole !== role) {
      if (userInput) userInput.value = '';
      if (passInput) passInput.value = '';
    }

    // Auto-fetch credentials if available
    if (role === 'student' && userInput && (!userInput.value || prevRole !== role)) {
      const savedEmail = localStorage.getItem('hms_last_student_email') || '';
      const savedPass = sessionStorage.getItem('hms_last_student_pass') || localStorage.getItem('hms_last_student_pass') || '';
      if (savedEmail) {
        userInput.value = savedEmail;
        if (passInput && savedPass) passInput.value = savedPass;
      }
    } else if (role === 'warden' && userInput && (!userInput.value || prevRole !== role)) {
      const savedEmail = localStorage.getItem('hms_last_warden_email') || 'priya123@gamil.com';
      const savedPass = sessionStorage.getItem('hms_last_warden_pass') || localStorage.getItem('hms_last_warden_pass') || 'priya123';
      if (savedEmail) {
        userInput.value = savedEmail;
        if (passInput && savedPass) passInput.value = savedPass;
      }
    } else if (role === 'admin' && userInput && (!userInput.value || prevRole !== role)) {
      const savedEmail = localStorage.getItem('hms_last_admin_email') || 'admin123@gmail.com';
      const savedPass = sessionStorage.getItem('hms_last_admin_pass') || localStorage.getItem('hms_last_admin_pass') || 'admin123';
      if (savedEmail) {
        userInput.value = savedEmail;
        if (passInput && savedPass) passInput.value = savedPass;
      }
    }

    if (userInput) {
      if (role === 'admin') userInput.placeholder = 'e.g. admin123@gmail.com';
      else if (role === 'warden') userInput.placeholder = 'e.g. priya123@gamil.com or WRD-207';
      else userInput.placeholder = 'e.g. STU-1011 or email';
    }

    if (hintText) {
      if (role === 'admin') {
        hintText.innerHTML = `<span>Sign in with Administrator credentials (admin123@gmail.com)</span>`;
      } else if (role === 'warden') {
        hintText.innerHTML = `<span>Sign in with Warden ID or assigned email (e.g. priya123@gamil.com)</span>`;
      } else {
        hintText.innerHTML = `<span>Sign in with Student ID or registered email</span>`;
      }
    }

    const errorAlert = document.getElementById('split-login-error');
    if (errorAlert) errorAlert.style.display = 'none';
  }

  toggleStudentAuthMode(mode) {
    const loginForm = document.getElementById('split-login-form');
    const registerForm = document.getElementById('split-register-form');
    const headerTitle = document.getElementById('card-header-title');
    const headerSubtitle = document.getElementById('card-header-subtitle');
    const btnSignin = document.getElementById('btn-mode-signin');
    const btnRegister = document.getElementById('btn-mode-register');

    if (mode === 'register') {
      if (loginForm) loginForm.style.display = 'none';
      if (registerForm) registerForm.style.display = 'block';
      if (headerTitle) headerTitle.innerText = 'Student Registration';
      if (headerSubtitle) headerSubtitle.innerText = 'Fill in your details to create a student account';
      if (btnSignin) btnSignin.classList.remove('active');
      if (btnRegister) btnRegister.classList.add('active');
    } else {
      if (loginForm) loginForm.style.display = 'block';
      if (registerForm) registerForm.style.display = 'none';
      if (headerTitle) headerTitle.innerText = 'Welcome back';
      if (headerSubtitle) headerSubtitle.innerText = 'Sign in to manage your hostel';
      if (btnSignin) btnSignin.classList.add('active');
      if (btnRegister) btnRegister.classList.remove('active');
    }
  }

  async handleLandingStudentRegister(event) {
    event.preventDefault();
    const form = document.getElementById('split-register-form');
    this.clearFormErrors(form);

    const nameInput = document.getElementById('land-reg-name');
    const emailInput = document.getElementById('land-reg-email');
    const passwordInput = document.getElementById('land-reg-password');
    const confirmInput = document.getElementById('land-reg-confirm-password');
    const phoneInput = document.getElementById('land-reg-phone');
    const deptInput = document.getElementById('land-reg-dept');
    const yearInput = document.getElementById('land-reg-year');
    const genderInput = document.getElementById('land-reg-gender');
    const emergencyInput = document.getElementById('land-reg-emergency');
    const needsInput = document.getElementById('land-reg-needs');

    const fullName = nameInput.value;
    const email = emailInput.value;
    const password = passwordInput.value;
    const confirmPassword = confirmInput ? confirmInput.value : password;
    const phone = phoneInput.value;
    const department = deptInput.value;
    const year = yearInput.value;
    const gender = genderInput.value;
    const emergencyContact = emergencyInput.value;
    const roomNeeds = needsInput.value;

    let hasError = false;

    const nameErr = Validator.validateName(fullName);
    if (nameErr) { this.showFieldError(nameInput, nameErr); hasError = true; }

    const emailErr = Validator.validateEmail(email);
    if (emailErr) { this.showFieldError(emailInput, emailErr); hasError = true; }

    const passErr = Validator.validatePassword(password);
    if (passErr) { this.showFieldError(passwordInput, passErr); hasError = true; }

    if (confirmInput) {
      const confirmErr = Validator.validateConfirmPassword(password, confirmPassword);
      if (confirmErr) { this.showFieldError(confirmInput, confirmErr); hasError = true; }
    }

    const phoneErr = Validator.validatePhone(phone);
    if (phoneErr) { this.showFieldError(phoneInput, phoneErr); hasError = true; }

    const deptErr = Validator.validateRequired(department, "Department");
    if (deptErr) { this.showFieldError(deptInput, deptErr); hasError = true; }

    const emergencyErr = Validator.validateRequired(emergencyContact, "Emergency contact");
    if (emergencyErr) { this.showFieldError(emergencyInput, emergencyErr); hasError = true; }

    const needsErr = Validator.validateRequired(roomNeeds, "Accommodation preferences");
    if (needsErr) { this.showFieldError(needsInput, needsErr); hasError = true; }

    if (hasError) {
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    try {
      const cleanPhone = phone.replace(/[\s-]/g, '');
      let cleanEmail = email.trim().toLowerCase();
      if (cleanEmail.endsWith('@gamil.com')) {
        cleanEmail = cleanEmail.replace('@gamil.com', '@gmail.com');
      }

      const newStudent = await store.addStudent({
        fullName, email: cleanEmail, password, phone: cleanPhone, department, year, gender, emergencyContact, roomNeeds
      });

      if (newStudent && newStudent.success === false) {
        this.showToast(newStudent.message || 'Registration failed', 'danger');
        if (newStudent.message && newStudent.message.toLowerCase().includes('email')) {
          this.showFieldError(emailInput, newStudent.message);
        }
        return;
      }

      const studentId = newStudent.id || newStudent.student?.id || '';

      // Persist credentials so they are fetched on the login form
      localStorage.setItem('hms_last_student_email', cleanEmail);
      sessionStorage.setItem('hms_last_student_pass', password);

      // Pre-fill credentials in the Sign In form
      const roleInput = document.getElementById('login-tab-role');
      if (roleInput) roleInput.value = 'student';
      this.setLoginTab('student');
      this.toggleStudentAuthMode('signin');

      const userInput = document.getElementById('split-username');
      const passInput = document.getElementById('split-password');
      if (userInput) userInput.value = cleanEmail;
      if (passInput) passInput.value = password;

      // Automatically sign in the registered student to fetch and show real dashboard data immediately!
      this.showToast(`Registration Successful! Logging in as ${fullName} (${studentId})...`, 'success');
      const loginRes = await store.login(cleanEmail, password, 'student');
      if (loginRes.success) {
        this.showToast(`Welcome to HostelHub, ${fullName}! (${studentId})`, 'success');
        this.showDashboardLayout();
        await this.switchRole('student');
        await this.switchView('student-room-view');
        return;
      }
    } catch (err) {
      this.showToast(err.message || 'Registration failed', 'danger');
    }
  }

  fillSplitDemoCredentials() {
    const role = document.getElementById('login-tab-role')?.value || 'admin';
    this.setLoginTab(role);
  }

  toggleSplitPassword() {
    const input = document.getElementById('split-password');
    const icon = document.getElementById('split-eye-icon');
    if (!input || !icon) return;

    if (input.type === 'password') {
      input.type = 'text';
      icon.className = 'fa-solid fa-eye-slash';
    } else {
      input.type = 'password';
      icon.className = 'fa-solid fa-eye';
    }
  }

  async handleSplitLogin(event) {
    event.preventDefault();
    const form = document.getElementById('split-login-form');
    this.clearFormErrors(form);

    const role = document.getElementById('login-tab-role').value;
    const identifierInput = document.getElementById('split-username');
    const passwordInput = document.getElementById('split-password');
    const identifier = identifierInput.value.trim();
    const password = passwordInput.value.trim();

    let hasError = false;
    if (!identifier) {
      this.showFieldError(identifierInput, "Username / ID / Email is required.");
      hasError = true;
    }
    if (!password) {
      this.showFieldError(passwordInput, "Password is required.");
      hasError = true;
    }

    if (hasError) return;

    const res = await store.login(identifier, password, role);
    if (res.success) {
      const errorAlert = document.getElementById('split-login-error');
      if (errorAlert) errorAlert.style.display = 'none';

      this.showToast(`Welcome back, ${res.user.name}! (${role.toUpperCase()} Dashboard Unlocked)`, 'success');
      this.showDashboardLayout();
      await this.switchRole(role);
    } else {
      const errorAlert = document.getElementById('split-login-error');
      const msgEl = document.getElementById('split-login-error-msg');
      if (errorAlert && msgEl) {
        msgEl.innerText = res.message;
        errorAlert.style.display = 'flex';
      }
      this.showToast(res.message, 'danger');
    }
  }

  // Theme Handling
  applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(btn => {
      btn.innerHTML = theme === 'dark' 
        ? `<span><i class="fa-solid fa-moon"></i> Dark Mode</span> <i class="fa-solid fa-circle-half-stroke"></i>`
        : `<span><i class="fa-solid fa-sun"></i> Light Mode</span> <i class="fa-solid fa-circle-half-stroke"></i>`;
    });
  }

  toggleTheme() {
    const newTheme = store.getTheme() === 'dark' ? 'light' : 'dark';
    store.setTheme(newTheme);
    this.applyTheme(newTheme);
    this.showToast(`Switched to ${newTheme} theme`, 'info');
  }

  // =========================================================================
  // PUBLIC LANDING PAGE RENDERING
  // =========================================================================
  renderPublicHome() {
    const metrics = store.getMetrics();
    const metricsGrid = document.getElementById('home-metrics-grid');
    if (metricsGrid) {
      metricsGrid.innerHTML = `
        <div class="metric-highlight-card">
          <div class="stat-icon indigo"><i class="fa-solid fa-percent"></i></div>
          <div>
            <div class="metric-val">${metrics.occupancyRate}%</div>
            <div class="metric-lbl">Occupancy Rate</div>
          </div>
        </div>
        <div class="metric-highlight-card">
          <div class="stat-icon rose"><i class="fa-solid fa-door-closed"></i></div>
          <div>
            <div class="metric-val">${metrics.bookedRooms}</div>
            <div class="metric-lbl">Booked / Filled Rooms</div>
          </div>
        </div>
        <div class="metric-highlight-card">
          <div class="stat-icon emerald"><i class="fa-solid fa-door-open"></i></div>
          <div>
            <div class="metric-val">${metrics.remainingRooms}</div>
            <div class="metric-lbl">Remaining Vacant Rooms</div>
          </div>
        </div>
        <div class="metric-highlight-card">
          <div class="stat-icon cyan"><i class="fa-solid fa-user-graduate"></i></div>
          <div>
            <div class="metric-val">${metrics.totalStudents}</div>
            <div class="metric-lbl">Registered Students</div>
          </div>
        </div>
      `;
    }

    this.renderPublicRooms();
    this.renderPublicRules();
  }

  renderPublicRooms() {
    const blockFilter = document.getElementById('public-room-block-filter')?.value || 'all';
    const statusFilter = document.getElementById('public-room-status-filter')?.value || 'all';

    let rooms = store.getRooms();
    if (blockFilter !== 'all') rooms = rooms.filter(r => r.block === blockFilter);
    if (statusFilter !== 'all') rooms = rooms.filter(r => r.status === statusFilter);

    const grid = document.getElementById('public-rooms-grid');
    if (!grid) return;

    if (rooms.length === 0) {
      grid.innerHTML = `<div class="card" style="grid-column: 1/-1; text-align: center; color: var(--text-muted);">No rooms found matching filters.</div>`;
    } else {
      grid.innerHTML = rooms.map(r => this.generateRoomCardHTML(r)).join('');
    }
  }

  renderPublicRules() {
    const search = document.getElementById('public-rule-search')?.value.toLowerCase() || '';
    let rules = store.getRules().filter(r => r.active);

    if (search) {
      rules = rules.filter(r => r.title.toLowerCase().includes(search) || r.description.toLowerCase().includes(search));
    }

    const grid = document.getElementById('public-rules-grid');
    if (!grid) return;

    if (rules.length === 0) {
      grid.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted);">No active rules matching search.</div>`;
      return;
    }

    grid.innerHTML = rules.map((r, idx) => `
      <div class="card rule-item-card">
        <div class="rule-number">${idx + 1}</div>
        <div class="rule-content">
          <div style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.35rem;">
            <h4>${r.title}</h4>
            <span class="badge badge-ac">${r.category}</span>
          </div>
          <p>${r.description}</p>
        </div>
      </div>
    `).join('');
  }

  // =========================================================================
  // AUTHENTICATION FLOWS & PASSWORD VALIDATION
  // =========================================================================
  openLoginModal(role = 'admin') {
    this.switchLoginModalTab(role);
    const alertBox = document.getElementById('login-error-alert');
    if (alertBox) alertBox.style.display = 'none';
    this.openModal('login-modal');
  }

  switchLoginModalTab(role) {
    document.querySelectorAll('.modal-tab-btn').forEach(btn => btn.classList.remove('active'));
    const activeBtn = document.getElementById(`modal-tab-${role}`);
    if (activeBtn) activeBtn.classList.add('active');

    const roleInput = document.getElementById('login-active-role');
    if (roleInput) roleInput.value = role;

    const labelEl = document.getElementById('login-identifier-label');
    const inputEl = document.getElementById('login-identifier');
    const passEl = document.getElementById('login-password');

    if (role === 'admin') {
      if (labelEl) labelEl.innerHTML = '<i class="fa-solid fa-envelope"></i> Admin Username / Email *';
      if (inputEl) { inputEl.placeholder = 'e.g. admin or admin@hms.edu'; inputEl.value = ''; }
      if (passEl) passEl.value = '';
    } else if (role === 'warden') {
      if (labelEl) labelEl.innerHTML = '<i class="fa-solid fa-id-badge"></i> Warden ID or Email *';
      if (inputEl) { inputEl.placeholder = 'e.g. WRD-204 or email'; inputEl.value = ''; }
      if (passEl) passEl.value = '';
    } else if (role === 'student') {
      if (labelEl) labelEl.innerHTML = '<i class="fa-solid fa-graduation-cap"></i> Student ID or Email *';
      if (inputEl) { inputEl.placeholder = 'e.g. STU-1008 or email'; inputEl.value = ''; }
      if (passEl) passEl.value = '';
    }
  }

  togglePasswordVisibility() {
    const input = document.getElementById('login-password');
    const icon = document.getElementById('password-eye-icon');
    if (!input || !icon) return;

    if (input.type === 'password') {
      input.type = 'text';
      icon.className = 'fa-solid fa-eye-slash';
    } else {
      input.type = 'password';
      icon.className = 'fa-solid fa-eye';
    }
  }

  async handlePortalLogin(event) {
    event.preventDefault();
    const role = document.getElementById('login-active-role').value;
    const identifier = document.getElementById('login-identifier').value;
    const password = document.getElementById('login-password').value;

    const res = await store.login(identifier, password, role);
    if (res.success) {
      this.closeModal('login-modal');
      const alertBox = document.getElementById('login-error-alert');
      if (alertBox) alertBox.style.display = 'none';

      this.showToast(`Welcome back, ${res.user.name}! (${role.toUpperCase()} Features Unlocked)`, 'success');
      this.showDashboardLayout();
      await this.switchRole(role);
    } else {
      const alertBox = document.getElementById('login-error-alert');
      const msgEl = document.getElementById('login-error-msg');
      if (alertBox && msgEl) {
        msgEl.innerText = res.message;
        alertBox.style.display = 'flex';
      }
      this.showToast(res.message, 'danger');
    }
  }

  handleLogout() {
    const role = store.getRole();
    store.logout();
    this.showToast('Logged out of session', 'info');
    this.showHomeLayout();
    if (role === 'student') {
      this.setLoginTab('student');
    } else if (role === 'warden') {
      this.setLoginTab('warden');
    } else if (role === 'admin') {
      this.setLoginTab('admin');
    }
  }

  logoutUser() {
    this.handleLogout();
  }

  renderUserProfileHeader() {
    const user = store.getCurrentUser();
    const container = document.getElementById('user-header-profile');
    if (!container) return;

    if (!user) {
      container.innerHTML = `
        <div class="user-profile-avatar"><i class="fa-solid fa-user"></i></div>
        <div class="user-profile-info">
          <span class="user-profile-name">Guest User</span>
          <span class="user-profile-role">Not Authenticated</span>
        </div>
      `;
      return;
    }

    const currentWarden = store.getCurrentWarden();
    const displayName = (user.role === 'warden' && currentWarden && currentWarden.name) ? currentWarden.name : user.name;
    const wardenBlock = (currentWarden && currentWarden.block) ? currentWarden.block : (user.block || 'Block C');
    const roleBadgeClass = user.role === 'admin' 
      ? 'Admin' 
      : user.role === 'warden' 
        ? `Warden (${wardenBlock})` 
        : `Student (${user.id})`;
    const avatarIcon = user.role === 'admin' ? 'fa-user-shield' : user.role === 'warden' ? 'fa-user-nurse' : 'fa-user-graduate';

    container.innerHTML = `
      <div class="user-profile-avatar"><i class="fa-solid ${avatarIcon}"></i></div>
      <div class="user-profile-info">
        <span class="user-profile-name">${displayName}</span>
        <span class="user-profile-role">${roleBadgeClass}</span>
      </div>
    `;
  }

  // =========================================================================
  // DASHBOARD ROLE & NAVIGATION CONTROLLER (RBAC GUARDED)
  // =========================================================================
  async switchRole(role) {
    store.setRole(role);
    this.renderUserProfileHeader();

    const roleTitle = document.getElementById('role-title-text');
    const roleSubtitle = document.getElementById('role-subtitle-text');
    const roleIcon = document.getElementById('role-avatar-icon');
    const studentSelectorWrap = document.getElementById('student-selector-wrap');

    if (role === 'admin') {
      roleTitle.innerText = 'System Admin';
      roleSubtitle.innerText = 'Full Management Control';
      roleIcon.innerHTML = '<i class="fa-solid fa-user-shield"></i>';
      if (studentSelectorWrap) studentSelectorWrap.style.display = 'none';
      this.renderSidebarMenu([
        { id: 'admin-overview-view', label: 'Overview', icon: 'fa-chart-pie' },
        { id: 'admin-rooms-view', label: 'Room Management', icon: 'fa-bed' },
        { id: 'admin-students-view', label: 'Student Directory', icon: 'fa-users' },
        { id: 'admin-wardens-view', label: 'Warden Officers', icon: 'fa-user-nurse' },
        { id: 'admin-rules-view', label: 'Rules & Policy Manager', icon: 'fa-scale-balanced' }
      ], 'admin-overview-view');
    } else if (role === 'warden') {
      roleTitle.innerText = 'Warden Officer';
      roleSubtitle.innerText = 'Discipline & Approvals';
      roleIcon.innerHTML = '<i class="fa-solid fa-user-nurse"></i>';
      if (studentSelectorWrap) studentSelectorWrap.style.display = 'none';
      this.renderSidebarMenu([
        { id: 'warden-leave-view', label: 'Leave Approvals', icon: 'fa-envelope-open-text' },
        { id: 'warden-complaints-view', label: 'Issue Rectification', icon: 'fa-screwdriver-wrench' },
        { id: 'warden-allocation-view', label: 'Smart Room Allocation', icon: 'fa-key' },
        { id: 'admin-students-view', label: 'Student Directory', icon: 'fa-users' },
        { id: 'student-rules-view', label: 'Hostel Rules', icon: 'fa-book-bookmark' }
      ], 'warden-leave-view');
    } else if (role === 'student') {
      roleTitle.innerText = 'Student Portal';
      roleSubtitle.innerText = 'Resident Services';
      roleIcon.innerHTML = '<i class="fa-solid fa-user-graduate"></i>';
      if (studentSelectorWrap) {
        studentSelectorWrap.style.display = 'none';
      }
      this.renderSidebarMenu([
        { id: 'student-room-view', label: 'My Room Details', icon: 'fa-bed' },
        { id: 'student-register-view', label: 'Registration & Needs Form', icon: 'fa-address-card' },
        { id: 'student-complaint-view', label: 'Raise Complaint Ticket', icon: 'fa-triangle-exclamation' },
        { id: 'student-leave-view', label: 'Request Letter / Leave', icon: 'fa-paper-plane' },
        { id: 'student-rules-view', label: 'Hostel Rules & Guide', icon: 'fa-book-bookmark' }
      ], 'student-room-view');
    }
  }

  renderStudentDropdown() {
    const select = document.getElementById('student-select');
    if (!select) return;
    const students = store.getStudents();
    select.innerHTML = students.map(s => `
      <option value="${s.id}" ${s.id === store.getStudentId() ? 'selected' : ''}>
        ${s.name} (${s.id})
      </option>
    `).join('');
  }

  renderSidebarMenu(items, defaultView) {
    const navMenu = document.getElementById('sidebar-nav-menu');
    if (!navMenu) return;

    navMenu.innerHTML = items.map(item => `
      <li class="nav-item ${item.id === defaultView ? 'active' : ''}" onclick="app.switchView('${item.id}')">
        <i class="fa-solid ${item.icon}"></i>
        <span>${item.label}</span>
      </li>
    `).join('');
    this.switchView(defaultView);
  }

  async switchView(viewId) {
    const user = store.getCurrentUser();

    // Route Guard Definitions by Role
    const roleRoutes = {
      admin: ['admin-overview-view', 'admin-rooms-view', 'admin-students-view', 'admin-wardens-view', 'admin-rules-view'],
      warden: ['warden-leave-view', 'warden-complaints-view', 'warden-allocation-view', 'admin-students-view', 'student-rules-view'],
      student: ['student-room-view', 'student-register-view', 'student-complaint-view', 'student-leave-view', 'student-rules-view']
    };

    if (user && roleRoutes[user.role] && !roleRoutes[user.role].includes(viewId)) {
      const fallbackView = roleRoutes[user.role][0];
      this.showToast(`Access Denied! ${user.role.toUpperCase()} role cannot access this feature.`, 'danger');
      viewId = fallbackView;
    }

    this.currentView = viewId;

    document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));

    const targetSection = document.getElementById(viewId);
    if (targetSection) targetSection.classList.add('active');

    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
      if (item.getAttribute('onclick') && item.getAttribute('onclick').includes(viewId)) {
        item.classList.add('active');
      }
    });

    const titleEl = document.getElementById('header-page-title');
    const subtitleEl = document.getElementById('header-page-subtitle');

    const titles = {
      'admin-overview-view': ['Dashboard Overview', 'Real-time hostel monitoring analytics'],
      'admin-rooms-view': ['Hostel Room Directory', 'Visual breakdown of booked vs remaining/vacant rooms'],
      'admin-students-view': ['Master Student Database', 'Resident student records and room status'],
      'admin-wardens-view': ['Warden Directory', 'Assigned hostel block wardens and contact details'],
      'admin-rules-view': ['Hostel Rules Manager', 'Configure code of conduct and student policies'],
      'warden-leave-view': ['Leave Request Approval Hub', 'Review student request letters and grant permissions'],
      'warden-complaints-view': ['Issue Rectification Desk', 'Manage and resolve reported student complaints'],
      'warden-allocation-view': ['Room Allocation Engine', 'Match unallocated student needs with remaining vacant rooms'],
      'student-register-view': ['Student Registration & Needs Form', 'Submit personal information and room accommodation preferences'],
      'student-room-view': ['My Accommodation Details', 'Current room booking status and roommate profile'],
      'student-complaint-view': ['Raise Complaint Ticket', 'Report maintenance issues directly to warden staff'],
      'student-leave-view': ['Request Letter / Leave Application', 'Submit out-of-campus leave letters for warden approval'],
      'student-rules-view': ['Hostel Rules & Regulations', 'Official residence guidelines and timing rules']
    };

    if (titles[viewId]) {
      if (titleEl) titleEl.innerText = titles[viewId][0];
      if (subtitleEl) subtitleEl.innerText = titles[viewId][1];
    }

    await this.renderCurrentViewContent(viewId);
  }

  async renderCurrentViewContent(viewId) {
    switch (viewId) {
      case 'admin-overview-view':
        this.renderAdminOverview();
        break;
      case 'admin-rooms-view':
        this.renderAdminRooms();
        break;
      case 'admin-students-view':
        this.renderAdminStudents();
        break;
      case 'admin-wardens-view':
        this.renderAdminWardens();
        break;
      case 'admin-rules-view':
        this.renderAdminRules();
        break;

      case 'warden-leave-view':
        this.renderWardenLeave();
        break;
      case 'warden-complaints-view':
        this.renderWardenComplaints();
        break;
      case 'warden-allocation-view':
        this.renderWardenAllocation();
        break;

      case 'student-register-view':
        await this.renderStudentRegisterForm();
        break;
      case 'student-room-view':
        await this.renderStudentRoomDetails();
        break;
      case 'student-complaint-view':
        this.renderStudentComplaintTracker();
        break;
      case 'student-leave-view':
        this.renderStudentLeaveTracker();
        break;
      case 'student-rules-view':
        this.renderStudentRules();
        break;
    }
  }

  // =========================================================================
  // RENDER ADMIN VIEWS
  // =========================================================================
  renderAdminOverview() {
    const metrics = store.getMetrics();
    const statsGrid = document.getElementById('admin-stats-grid');
    if (!statsGrid) return;

    statsGrid.innerHTML = `
      <div class="card stat-card">
        <div class="stat-icon indigo"><i class="fa-solid fa-percent"></i></div>
        <div class="stat-info">
          <h3>${metrics.occupancyRate}%</h3>
          <p>Occupancy Rate</p>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon emerald"><i class="fa-solid fa-door-open"></i></div>
        <div class="stat-info">
          <h3>${metrics.remainingRooms} / ${metrics.totalRooms}</h3>
          <p>Remaining Vacant Rooms</p>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon cyan"><i class="fa-solid fa-user-graduate"></i></div>
        <div class="stat-info">
          <h3>${metrics.totalStudents}</h3>
          <p>Total Registered Students</p>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon amber"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <div class="stat-info">
          <h3>${metrics.pendingComplaints}</h3>
          <p>Open Complaint Tickets</p>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon rose"><i class="fa-solid fa-envelope-open-text"></i></div>
        <div class="stat-info">
          <h3>${metrics.pendingLeave}</h3>
          <p>Pending Leave Letters</p>
        </div>
      </div>
    `;

    const rooms = store.getRooms().slice(0, 6);
    const quickRoomsGrid = document.getElementById('admin-quick-rooms-grid');
    if (quickRoomsGrid) {
      quickRoomsGrid.innerHTML = rooms.map(r => this.generateRoomCardHTML(r)).join('');
    }

    const auditList = document.getElementById('admin-audit-list');
    if (auditList) {
      const complaints = store.getComplaints().slice(0, 3);
      const leave = store.getLeaveRequests().slice(0, 3);

      auditList.innerHTML = `
        <div style="font-size: 0.85rem; display: flex; flex-direction: column; gap: 0.75rem;">
          <div style="font-weight: 700; color: var(--text-muted); font-size: 0.75rem; text-transform: uppercase;">Recent Complaints</div>
          ${complaints.map(c => `
            <div style="padding: 0.6rem; background: var(--bg-secondary); border-radius: var(--radius-sm); display: flex; justify-content: space-between; align-items: center;">
              <div>
                <strong>${c.category} (${c.room})</strong>
                <div style="font-size: 0.75rem; color: var(--text-muted);">${c.studentName}</div>
              </div>
              <span class="badge badge-${c.status === 'Resolved' ? 'resolved' : c.status === 'In Progress' ? 'progress' : 'pending'}">${c.status}</span>
            </div>
          `).join('')}

          <div style="font-weight: 700; color: var(--text-muted); font-size: 0.75rem; text-transform: uppercase; margin-top: 0.5rem;">Recent Leave Applications</div>
          ${leave.map(l => `
            <div style="padding: 0.6rem; background: var(--bg-secondary); border-radius: var(--radius-sm); display: flex; justify-content: space-between; align-items: center;">
              <div>
                <strong>${l.studentName}</strong> - ${l.leaveType}
                <div style="font-size: 0.75rem; color: var(--text-muted);">${l.startDate} to ${l.endDate}</div>
              </div>
              <span class="badge badge-${l.status.toLowerCase()}">${l.status}</span>
            </div>
          `).join('')}
        </div>
      `;
    }
  }

  generateRoomCardHTML(room) {
    const isBooked = room.status === 'booked' || room.status === 'FULL';
    const occupants = room.occupants ? room.occupants.map(id => {
      const s = store.getStudent(id);
      return s ? s.name : id;
    }) : [];

    return `
      <div class="card room-card ${isBooked ? 'booked' : 'remaining'}">
        <div class="room-header">
          <div class="room-number">
            <i class="fa-solid fa-door-closed"></i> Room ${room.number || room.id}
            <span style="font-size: 0.75rem; color: var(--text-muted);">(${room.block})</span>
          </div>
          <span class="badge badge-${isBooked ? 'booked' : 'remaining'}">${isBooked ? 'Booked' : 'Remaining / Vacant'}</span>
        </div>
        <div class="room-details">
          <div><i class="fa-solid fa-layer-group"></i> Floor: ${room.floor}</div>
          <div><i class="fa-solid fa-cube"></i> Type: ${room.type}</div>
          <div><i class="fa-solid fa-users"></i> Capacity: ${occupants.length} / ${room.capacity} slots filled</div>
        </div>
        <div class="room-occupants">
          <div style="font-weight: 600; font-size: 0.75rem; color: var(--text-muted);">Occupant Students:</div>
          ${occupants.length > 0 
            ? occupants.map(o => `<span class="occupant-pill"><i class="fa-solid fa-user-graduate"></i> ${o}</span>`).join('') 
            : '<span style="font-size: 0.78rem; color: var(--success);"><i class="fa-solid fa-circle-check"></i> Vacant & Remaining</span>'}
        </div>
        ${store.getRole() === 'admin' ? `
          <div style="margin-top: 0.75rem; text-align: right;">
            <button class="btn btn-danger btn-sm" onclick="app.handleDeleteRoom('${room.id}')"><i class="fa-solid fa-trash"></i> Delete Room</button>
          </div>
        ` : ''}
      </div>
    `;
  }

  renderAdminRooms() {
    const search = document.getElementById('admin-room-search')?.value.toLowerCase() || '';
    const blockFilter = document.getElementById('admin-room-block-filter')?.value || 'all';
    const statusFilter = document.getElementById('admin-room-status-filter')?.value || 'all';

    let rooms = store.getRooms();

    if (blockFilter !== 'all') rooms = rooms.filter(r => r.block === blockFilter);
    if (statusFilter !== 'all') rooms = rooms.filter(r => r.status === statusFilter);

    if (search) {
      rooms = rooms.filter(r => 
        (r.number && r.number.toLowerCase().includes(search)) || 
        (r.id && r.id.toLowerCase().includes(search)) ||
        (r.block && r.block.toLowerCase().includes(search)) ||
        (r.occupants && r.occupants.some(id => {
          const s = store.getStudent(id);
          return s && s.name.toLowerCase().includes(search);
        }))
      );
    }

    const grid = document.getElementById('admin-all-rooms-grid');
    if (!grid) return;

    if (rooms.length === 0) {
      grid.innerHTML = `<div class="card" style="grid-column: 1/-1; text-align: center; color: var(--text-muted);">No rooms found matching filters.</div>`;
    } else {
      grid.innerHTML = rooms.map(r => this.generateRoomCardHTML(r)).join('');
    }
  }

  async handleAddRoom(event) {
    event.preventDefault();
    const modal = document.getElementById('add-room-modal');
    const form = modal ? modal.querySelector('form') : null;
    this.clearFormErrors(form);

    const blockInput = document.getElementById('room-block');
    const numInput = document.getElementById('room-number');
    const floorInput = document.getElementById('room-floor');
    const typeInput = document.getElementById('room-type');
    const capInput = document.getElementById('room-capacity');

    const block = blockInput.value;
    const number = numInput.value;
    const floor = parseInt(floorInput.value, 10);
    const type = typeInput.value;
    const capacity = parseInt(capInput.value, 10);

    const roomErr = Validator.validateRoom(number, floor, capacity);
    if (roomErr) {
      if (roomErr.field === 'number') this.showFieldError(numInput, roomErr.message);
      if (roomErr.field === 'floor') this.showFieldError(floorInput, roomErr.message);
      if (roomErr.field === 'capacity') this.showFieldError(capInput, roomErr.message);
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    const res = await store.addRoom({ block, number, floor, type, capacity });
    if (res.success) {
      this.closeModal('add-room-modal');
      this.showToast(`Room ${number} added to ${block}!`, 'success');
      this.renderAdminRooms();
      this.renderPublicRooms();
    } else {
      this.showToast(res.message || 'Failed to add room', 'danger');
      if (res.message && res.message.toLowerCase().includes('room')) {
        this.showFieldError(numInput, res.message);
      }
    }
  }

  async handleDeleteRoom(roomId) {
    if (confirm(`Are you sure you want to delete Room ${roomId}?`)) {
      const res = await store.deleteRoom(roomId);
      if (res.success) {
        this.showToast(`Room ${roomId} deleted.`, 'success');
        this.renderAdminRooms();
        this.renderPublicRooms();
      } else {
        this.showToast(res.message, 'danger');
      }
    }
  }

  renderAdminStudents() {
    const search = document.getElementById('admin-student-search')?.value.toLowerCase() || '';
    const statusFilter = document.getElementById('admin-student-filter')?.value || 'all';

    let students = store.getStudents();

    if (statusFilter !== 'all') students = students.filter(s => s.status === statusFilter);

    if (search) {
      students = students.filter(s => 
        (s.name && s.name.toLowerCase().includes(search)) ||
        (s.email && s.email.toLowerCase().includes(search)) ||
        (s.dept && s.dept.toLowerCase().includes(search)) ||
        (s.assignedRoom && s.assignedRoom.toLowerCase().includes(search))
      );
    }

    const tbody = document.getElementById('admin-students-table-body');
    if (!tbody) return;

    if (students.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No students found.</td></tr>`;
      return;
    }

    tbody.innerHTML = students.map(s => `
      <tr>
        <td>
          <strong>${s.name}</strong>
          <div style="font-size: 0.75rem; color: var(--text-muted);">${s.id} | ${s.email}</div>
        </td>
        <td>${s.dept}<br><span style="font-size: 0.75rem; color: var(--text-muted);">${s.year}</span></td>
        <td>
          ${s.assignedRoom && s.assignedRoom !== 'Unassigned' 
            ? `<span class="badge badge-ac"><i class="fa-solid fa-bed"></i> Room ${s.assignedRoom}</span>` 
            : `<span class="badge badge-pending">Unassigned</span>`}
        </td>
        <td style="max-width: 200px; font-size: 0.8rem; color: var(--text-muted);">${s.needs || 'No specific needs'}</td>
        <td style="font-size: 0.8rem;">${s.emergencyContact || 'N/A'}</td>
        <td>
          <span class="badge badge-${s.status === 'Allocated' ? 'approved' : 'pending'}">${s.status}</span>
        </td>
        <td>
          ${s.assignedRoom && s.assignedRoom !== 'Unassigned' 
            ? `<button class="btn btn-danger btn-sm" onclick="app.vacateStudent('${s.id}')"><i class="fa-solid fa-user-minus"></i> Vacate</button>`
            : `<button class="btn btn-success btn-sm" onclick="app.openAllocateModal('${s.id}')"><i class="fa-solid fa-key"></i> Allocate</button>`}
        </td>
      </tr>
    `).join('');
  }

  async vacateStudent(studentId) {
    if (confirm('Are you sure you want to vacate this student from their room?')) {
      const res = await store.vacateStudent(studentId);
      if (res) {
        this.showToast('Student room vacated successfully', 'success');
        this.renderAdminStudents();
        this.renderAdminRooms();
      } else {
        this.showToast('Failed to vacate student room', 'danger');
      }
    }
  }

  renderAdminWardens() {
    const wardens = store.getWardens();
    const grid = document.getElementById('admin-wardens-grid');
    if (!grid) return;

    if (!wardens || wardens.length === 0) {
      grid.innerHTML = `
        <div class="card" style="grid-column: 1 / -1; text-align: center; color: var(--text-muted); padding: 2.5rem;">
          <i class="fa-solid fa-user-nurse" style="font-size: 2.5rem; margin-bottom: 0.75rem; color: var(--text-muted); opacity: 0.5;"></i>
          <h3>No Warden Officers Registered</h3>
          <p style="font-size: 0.85rem; margin-top: 0.25rem;">Click "Add Warden Officer" above to assign an officer to a hostel block.</p>
        </div>
      `;
      return;
    }

    grid.innerHTML = wardens.map(w => `
      <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
        <div>
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
            <div style="display: flex; gap: 0.85rem; align-items: center;">
              <div class="role-avatar" style="width: 48px; height: 48px; font-size: 1.3rem;">
                <i class="fa-solid fa-user-nurse"></i>
              </div>
              <div>
                <h3 style="font-size: 1.1rem;">${w.name}</h3>
                <span class="badge badge-ac">${w.block} Chief Warden</span>
              </div>
            </div>
            <span class="badge badge-approved">${w.status || 'Active'}</span>
          </div>
          <div style="font-size: 0.85rem; color: var(--text-muted); display: flex; flex-direction: column; gap: 0.4rem;">
            <div><i class="fa-solid fa-id-badge"></i> Officer ID: <strong>${w.id}</strong></div>
            <div><i class="fa-solid fa-envelope"></i> Email: ${w.email}</div>
            <div><i class="fa-solid fa-phone"></i> Contact: ${w.phone}</div>
          </div>
        </div>
        <div style="margin-top: 1.25rem; padding-top: 0.85rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end;">
          <button class="btn btn-danger btn-sm" onclick="app.removeWarden('${w.id}', '${w.name.replace(/'/g, "\\'")}')">
            <i class="fa-solid fa-trash-can"></i> Remove Warden
          </button>
        </div>
      </div>
    `).join('');
  }

  renderAdminRules() {
    const rules = store.getRules();
    const list = document.getElementById('admin-rules-list');
    if (!list) return;

    list.innerHTML = rules.map((r, idx) => `
      <div class="card rule-item-card">
        <div class="rule-number">${idx + 1}</div>
        <div class="rule-content" style="flex: 1;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
            <h4>${r.title}</h4>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span class="badge badge-ac">${r.category}</span>
              <button class="btn btn-${r.active ? 'secondary' : 'primary'} btn-sm" onclick="app.toggleRule('${r.id}')">
                ${r.active ? '<i class="fa-solid fa-eye-slash"></i> Disable' : '<i class="fa-solid fa-eye"></i> Enable'}
              </button>
              <button class="btn btn-danger btn-sm" onclick="app.deleteRule('${r.id}')"><i class="fa-solid fa-trash"></i></button>
            </div>
          </div>
          <p>${r.description}</p>
        </div>
      </div>
    `).join('');
  }

  async toggleRule(id) {
    await store.toggleRule(id);
    this.renderAdminRules();
    this.renderPublicRules();
    this.showToast('Rule visibility updated', 'info');
  }

  async deleteRule(id) {
    if (confirm('Delete this hostel rule?')) {
      await store.deleteRule(id);
      this.renderAdminRules();
      this.renderPublicRules();
      this.showToast('Rule deleted', 'danger');
    }
  }

  // =========================================================================
  // RENDER WARDEN VIEWS
  // =========================================================================
  renderWardenLeave() {
    // Render Warden Officer Details Banner
    const currentWarden = store.getCurrentWarden();
    const currentUser = store.getCurrentUser();
    const wName = (currentWarden && currentWarden.name) || (currentUser && currentUser.name) || 'Warden Officer';
    const wBlock = (currentWarden && currentWarden.block) || (currentUser && currentUser.block) || 'Block C';
    const wId = (currentWarden && currentWarden.id) || (currentUser && currentUser.id) || 'WRD-207';
    const wEmail = (currentWarden && currentWarden.email) || (currentUser && currentUser.email) || '';
    const wPhone = (currentWarden && currentWarden.phone) || (currentUser && currentUser.phone) || '';

    const nameEl = document.getElementById('warden-dashboard-name');
    const metaEl = document.getElementById('warden-dashboard-meta');
    const blockBadge = document.getElementById('warden-dashboard-block-badge');
    const idBadge = document.getElementById('warden-dashboard-id-badge');

    if (nameEl) nameEl.innerText = `${wName} (Hostel Warden)`;
    if (metaEl) metaEl.innerText = `Assigned Block: ${wBlock} | Email: ${wEmail}${wPhone ? ' | Phone: ' + wPhone : ''}`;
    if (blockBadge) blockBadge.innerHTML = `<i class="fa-solid fa-building"></i> ${wBlock}`;
    if (idBadge) idBadge.innerHTML = `<i class="fa-solid fa-id-badge"></i> ${wId}`;

    const filter = document.getElementById('warden-leave-filter')?.value || 'Pending';
    let requests = store.getLeaveRequests();

    if (filter !== 'all') requests = requests.filter(r => r.status === filter);

    const tbody = document.getElementById('warden-leave-table-body');
    if (!tbody) return;

    if (requests.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No leave requests found for this filter.</td></tr>`;
      return;
    }

    tbody.innerHTML = requests.map(r => `
      <tr>
        <td>
          <strong>${r.studentName}</strong>
          <div style="font-size: 0.75rem; color: var(--text-muted);">Room: ${r.room} | ID: ${r.studentId}</div>
        </td>
        <td><span class="badge badge-ac">${r.leaveType}</span></td>
        <td style="font-size: 0.82rem;">${r.startDate}<br>to ${r.endDate}</td>
        <td style="max-width: 220px; font-size: 0.82rem; color: var(--text-muted);">${r.reason}</td>
        <td style="font-size: 0.82rem;">${r.parentContact}</td>
        <td><span class="badge badge-${(r.status || 'Pending').toLowerCase()}">${r.status}</span></td>
        <td>
          <button class="btn btn-primary btn-sm" onclick="app.openLeaveActionModal('${r.id}')">
            <i class="fa-solid fa-pen-to-square"></i> Review
          </button>
        </td>
      </tr>
    `).join('');
  }

  openLeaveActionModal(reqId) {
    const req = store.getLeaveRequests().find(r => r.id === reqId);
    if (!req) return;

    document.getElementById('leave-action-id').value = req.id;
    document.getElementById('leave-modal-details').innerHTML = `
      <div style="background: var(--bg-primary); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1rem;">
        <div><strong>Student:</strong> ${req.studentName} (Room ${req.room})</div>
        <div><strong>Leave Type:</strong> ${req.leaveType} (${req.startDate} to ${req.endDate})</div>
        <div style="margin-top: 0.35rem;"><strong>Reason:</strong> ${req.reason}</div>
        <div><strong>Parent Contact:</strong> ${req.parentContact}</div>
      </div>
    `;

    document.getElementById('leave-action-status').value = req.status === 'Pending' ? 'Approved' : req.status;
    document.getElementById('leave-action-notes').value = req.wardenNotes || '';
    this.openModal('warden-leave-modal');
  }

  async handleProcessLeave(event) {
    event.preventDefault();
    const modal = document.getElementById('warden-leave-modal');
    const form = modal ? modal.querySelector('form') : null;
    this.clearFormErrors(form);

    const reqId = document.getElementById('leave-action-id').value;
    const statusInput = document.getElementById('leave-action-status');
    const notesInput = document.getElementById('leave-action-notes');
    const status = statusInput.value;
    const notes = notesInput.value;

    if (!notes || !notes.trim()) {
      this.showFieldError(notesInput, "Warden remarks/notes are required.");
      this.showToast("Please provide warden remarks before submitting decision.", "danger");
      return;
    }

    const res = await store.updateLeaveStatus(reqId, status, notes);
    if (res && res.success !== false) {
      this.closeModal('warden-leave-modal');
      this.showToast(`Leave request ${status.toLowerCase()} successfully`, status === 'Approved' ? 'success' : 'danger');
      this.renderWardenLeave();
    } else {
      this.showToast(res?.message || 'Failed to process leave request', 'danger');
    }
  }

  renderWardenComplaints() {
    const filter = document.getElementById('warden-complaint-filter')?.value || 'all';
    let complaints = store.getComplaints();

    if (filter !== 'all') complaints = complaints.filter(c => c.status === filter);

    const tbody = document.getElementById('warden-complaints-table-body');
    if (!tbody) return;

    if (complaints.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No complaint tickets match criteria.</td></tr>`;
      return;
    }

    tbody.innerHTML = complaints.map(c => `
      <tr>
        <td><strong>${c.id}</strong></td>
        <td>
          <strong>${c.studentName}</strong>
          <div style="font-size: 0.75rem; color: var(--text-muted);">Room: ${c.room}</div>
        </td>
        <td><span class="badge badge-ac">${c.category}</span></td>
        <td style="max-width: 220px; font-size: 0.82rem; color: var(--text-muted);">${c.description}</td>
        <td>
          <span class="badge badge-${c.priority === 'High' ? 'booked' : 'pending'}">${c.priority}</span>
        </td>
        <td>
          <span class="badge badge-${c.status === 'Resolved' ? 'resolved' : c.status === 'In Progress' ? 'progress' : 'pending'}">${c.status}</span>
        </td>
        <td style="font-size: 0.8rem; color: var(--text-muted);">${c.remarks || 'No remarks added'}</td>
        <td>
          <button class="btn btn-primary btn-sm" onclick="app.openComplaintActionModal('${c.id}')">
            <i class="fa-solid fa-wrench"></i> Update
          </button>
        </td>
      </tr>
    `).join('');
  }

  openComplaintActionModal(cmpId) {
    const cmp = store.getComplaints().find(c => c.id === cmpId);
    if (!cmp) return;

    document.getElementById('cmp-action-id').value = cmp.id;
    document.getElementById('cmp-modal-details').innerHTML = `
      <div style="background: var(--bg-primary); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1rem;">
        <div><strong>Ticket ${cmp.id}:</strong> ${cmp.category}</div>
        <div><strong>Student:</strong> ${cmp.studentName} (Room ${cmp.room})</div>
        <div style="margin-top: 0.35rem;"><strong>Issue:</strong> ${cmp.description}</div>
      </div>
    `;

    document.getElementById('cmp-action-status').value = cmp.status;
    document.getElementById('cmp-action-remarks').value = cmp.remarks || '';
    this.openModal('warden-complaint-modal');
  }

  async handleProcessComplaint(event) {
    event.preventDefault();
    const modal = document.getElementById('warden-complaint-modal');
    const form = modal ? modal.querySelector('form') : null;
    this.clearFormErrors(form);

    const cmpId = document.getElementById('cmp-action-id').value;
    const statusInput = document.getElementById('cmp-action-status');
    const remarksInput = document.getElementById('cmp-action-remarks');
    const status = statusInput.value;
    const remarks = remarksInput.value;

    if (!remarks || !remarks.trim()) {
      this.showFieldError(remarksInput, "Warden/technician remarks are required.");
      this.showToast("Please provide remarks before saving complaint status.", "danger");
      return;
    }

    const res = await store.updateComplaintStatus(cmpId, status, remarks);
    if (res && res.success !== false) {
      this.closeModal('warden-complaint-modal');
      this.showToast(`Complaint status updated to ${status}`, 'success');
      this.renderWardenComplaints();
    } else {
      this.showToast(res?.message || 'Failed to update complaint', 'danger');
    }
  }

  renderWardenAllocation() {
    const unallocated = store.getStudents().filter(s => !s.assignedRoom || s.assignedRoom === 'Unassigned');
    const remainingRooms = store.getRooms().filter(r => r.status === 'remaining' || r.status === 'VACANT' || r.status === 'PARTIALLY_OCCUPIED');

    const unallocatedContainer = document.getElementById('warden-unallocated-list');
    if (unallocatedContainer) {
      if (unallocated.length === 0) {
        unallocatedContainer.innerHTML = `<div class="card" style="text-align: center; color: var(--success);"><i class="fa-solid fa-circle-check"></i> All registered students have room allocations!</div>`;
      } else {
        unallocatedContainer.innerHTML = unallocated.map(s => `
          <div class="card" style="border-left: 4px solid var(--warning); padding: 1rem;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div>
                <strong>${s.name}</strong> (${s.id})
                <div style="font-size: 0.78rem; color: var(--text-muted);">${s.dept} - ${s.year}</div>
              </div>
              <button class="btn btn-success btn-sm" onclick="app.openAllocateModal('${s.id}')">
                <i class="fa-solid fa-key"></i> Allocate Room
              </button>
            </div>
            <div style="margin-top: 0.75rem; background: var(--bg-primary); padding: 0.6rem; border-radius: var(--radius-sm); font-size: 0.8rem;">
              <strong style="color: var(--warning);"><i class="fa-solid fa-heart-pulse"></i> Needs & Preferences:</strong> ${s.needs || 'None specified'}
            </div>
          </div>
        `).join('');
      }
    }

    const remainingGrid = document.getElementById('warden-remaining-rooms-grid');
    if (remainingGrid) {
      if (remainingRooms.length === 0) {
        remainingGrid.innerHTML = `<div class="card" style="grid-column: 1/-1; text-align: center; color: var(--danger);">No vacant remaining rooms available.</div>`;
      } else {
        remainingGrid.innerHTML = remainingRooms.map(r => this.generateRoomCardHTML(r)).join('');
      }
    }
  }

  openAllocateModal(studentId) {
    const student = store.getStudent(studentId);
    const remainingRooms = store.getRooms().filter(r => r.status === 'remaining' || r.status === 'VACANT' || r.status === 'PARTIALLY_OCCUPIED');

    if (!student) return;

    if (remainingRooms.length === 0) {
      this.showToast('No remaining rooms available for allocation!', 'danger');
      return;
    }

    document.getElementById('alloc-student-id').value = student.id;
    document.getElementById('alloc-modal-student-info').innerHTML = `
      <div style="background: var(--bg-primary); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1rem;">
        <div><strong>Student:</strong> ${student.name} (${student.id})</div>
        <div><strong>Department:</strong> ${student.dept} | ${student.year}</div>
        <div style="margin-top: 0.35rem; color: var(--warning);"><strong>Stated Needs:</strong> ${student.needs || 'None'}</div>
      </div>
    `;

    const select = document.getElementById('alloc-room-select');
    select.innerHTML = remainingRooms.map(r => `
      <option value="${r.id}">
        Room ${r.number || r.id} (${r.block}, Floor ${r.floor}) - ${r.type} [${r.capacity} slot(s)]
      </option>
    `).join('');

    this.openModal('allocate-room-modal');
  }

  async handleConfirmAllocation(event) {
    event.preventDefault();
    const modal = document.getElementById('allocate-room-modal');
    const form = modal ? modal.querySelector('form') : null;
    this.clearFormErrors(form);

    const studentId = document.getElementById('alloc-student-id').value;
    const roomSelect = document.getElementById('alloc-room-select');
    const roomId = roomSelect ? roomSelect.value : '';

    if (!roomId) {
      this.showFieldError(roomSelect, "Please select a vacant room to allocate.");
      this.showToast("Please select a room to confirm allocation.", "danger");
      return;
    }

    const res = await store.allocateRoom(roomId, studentId);
    if (res && res.success !== false) {
      this.closeModal('allocate-room-modal');
      this.showToast(`Room ${roomId} allocated to student successfully!`, 'success');
      this.renderWardenAllocation();
      this.renderAdminStudents();
      this.renderAdminRooms();
      this.renderPublicRooms();
    } else {
      this.showToast(res?.message || 'Room allocation failed', 'danger');
    }
  }

  // =========================================================================
  // RENDER STUDENT VIEWS
  // =========================================================================
  async renderStudentRegisterForm() {
    let currentStudent = store.getCurrentStudent();
    if (!currentStudent && store.getRole() === 'student') {
      currentStudent = await store.fetchCurrentStudent();
    }
    if (!currentStudent) return;

    const nameEl = document.getElementById('reg-name');
    const emailEl = document.getElementById('reg-email');
    const phoneEl = document.getElementById('reg-phone');
    const deptEl = document.getElementById('reg-dept');
    const yearEl = document.getElementById('reg-year');
    const genderEl = document.getElementById('reg-gender');
    const emergencyEl = document.getElementById('reg-emergency');
    const needsEl = document.getElementById('reg-needs');

    if (nameEl) nameEl.value = currentStudent.name || currentStudent.fullName || '';
    if (emailEl) emailEl.value = currentStudent.email || '';
    if (phoneEl) phoneEl.value = currentStudent.phone || '';
    if (deptEl) deptEl.value = currentStudent.dept || currentStudent.department || '';
    if (yearEl) yearEl.value = currentStudent.year || currentStudent.studentYear || '1st Year';
    if (genderEl) genderEl.value = currentStudent.gender || 'Male';
    if (emergencyEl) emergencyEl.value = currentStudent.emergencyContact || '';
    if (needsEl) needsEl.value = currentStudent.needs || currentStudent.roomNeeds || '';
  }

  async handleStudentRegister(event) {
    event.preventDefault();
    const form = document.getElementById('student-registration-form');
    this.clearFormErrors(form);

    const nameInput = document.getElementById('reg-name');
    const emailInput = document.getElementById('reg-email');
    const phoneInput = document.getElementById('reg-phone');
    const deptInput = document.getElementById('reg-dept');
    const yearInput = document.getElementById('reg-year');
    const genderInput = document.getElementById('reg-gender');
    const emergencyInput = document.getElementById('reg-emergency');
    const needsInput = document.getElementById('reg-needs');

    const name = nameInput.value;
    const email = emailInput.value;
    const phone = phoneInput.value;
    const dept = deptInput.value;
    const year = yearInput.value;
    const gender = genderInput.value;
    const emergencyContact = emergencyInput.value;
    const needs = needsInput.value;

    let hasError = false;

    const nameErr = Validator.validateName(name);
    if (nameErr) { this.showFieldError(nameInput, nameErr); hasError = true; }

    const emailErr = Validator.validateEmail(email);
    if (emailErr) { this.showFieldError(emailInput, emailErr); hasError = true; }

    const phoneErr = Validator.validatePhone(phone);
    if (phoneErr) { this.showFieldError(phoneInput, phoneErr); hasError = true; }

    const deptErr = Validator.validateRequired(dept, "Department");
    if (deptErr) { this.showFieldError(deptInput, deptErr); hasError = true; }

    const emergencyErr = Validator.validateRequired(emergencyContact, "Emergency contact");
    if (emergencyErr) { this.showFieldError(emergencyInput, emergencyErr); hasError = true; }

    const needsErr = Validator.validateRequired(needs, "Accommodation preferences");
    if (needsErr) { this.showFieldError(needsInput, needsErr); hasError = true; }

    if (hasError) {
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    const cleanPhone = phone.replace(/[\s-]/g, '');
    let existingStudent = store.getCurrentStudent();
    if (!existingStudent && store.getRole() === 'student') {
      existingStudent = await store.fetchCurrentStudent();
    }

    if (existingStudent) {
      const res = await store.updateStudent(existingStudent.id, {
        name, email, phone: cleanPhone, dept, year, gender, emergencyContact, needs
      });
      if (res && res.success === false) {
        this.showToast(res.message || 'Failed to update student profile', 'danger');
        return;
      }
      this.showToast('Student profile and preferences updated successfully!', 'success');
    } else {
      const newStudent = await store.addStudent({
        fullName: name, email, phone: cleanPhone, department: dept, year, gender, emergencyContact, roomNeeds: needs
      });
      if (newStudent && newStudent.success === false) {
        this.showToast(newStudent.message || 'Registration failed', 'danger');
        return;
      }
      this.renderStudentDropdown();
      this.showToast(`Registered successfully! Your ID is ${newStudent.id || newStudent.student?.id}`, 'success');
    }

    await this.switchView('student-room-view');
  }

  async renderStudentRoomDetails() {
    let student = store.getCurrentStudent();
    if (!student && store.getRole() === 'student') {
      student = await store.fetchCurrentStudent();
    }
    const container = document.getElementById('student-room-details-card');
    if (!container) return;

    if (!student) {
      container.innerHTML = `
        <div class="card" style="text-align: center; padding: 2rem;">
          <i class="fa-solid fa-spinner fa-spin" style="font-size: 2rem; color: var(--primary);"></i>
          <p style="margin-top: 1rem; color: var(--text-muted);">Loading your student profile...</p>
        </div>
      `;
      return;
    }

    if (!student.assignedRoom || student.assignedRoom === 'Unassigned') {
      container.innerHTML = `
        <div class="card" style="border-left: 4px solid var(--warning);">
          <div style="display: flex; gap: 1.25rem; align-items: center; margin-bottom: 1rem;">
            <div class="stat-icon amber" style="width: 60px; height: 60px; font-size: 1.8rem;">
              <i class="fa-solid fa-clock"></i>
            </div>
            <div>
              <h3>Room Allocation Pending</h3>
              <p style="color: var(--text-muted);">Your accommodation request is currently under review by the block warden.</p>
            </div>
          </div>
          <div style="background: var(--bg-primary); padding: 1.25rem; border-radius: var(--radius-md);">
            <div style="font-weight: 700; color: var(--primary); margin-bottom: 0.5rem;"><i class="fa-solid fa-heart"></i> Your Stated Accommodation Needs:</div>
            <p style="color: var(--text-muted); font-size: 0.9rem;">"${student.needs || 'No specific preferences provided.'}"</p>
          </div>
          <div style="margin-top: 1.25rem; display: flex; gap: 0.75rem;">
            <button class="btn btn-secondary" onclick="app.switchView('student-register-view')">
              <i class="fa-solid fa-pen-to-square"></i> Update Profile & Needs
            </button>
          </div>
        </div>
      `;
    } else {
      const room = store.getRoom(student.assignedRoom);
      const roommates = room && room.occupants ? room.occupants
        .filter(id => id !== student.id)
        .map(id => store.getStudent(id))
        .filter(Boolean) : [];

      container.innerHTML = `
        <div class="card" style="border-left: 4px solid var(--success);">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem;">
            <div>
              <span class="badge badge-approved" style="margin-bottom: 0.5rem; display: inline-block;">Allocated & Active</span>
              <h2 style="font-size: 2rem; font-family: 'Outfit';"><i class="fa-solid fa-door-open"></i> Room ${room ? (room.number || room.id) : student.assignedRoom}</h2>
              <p style="color: var(--text-muted);">${room ? room.block : 'Hostel Block'} | Floor ${room ? room.floor : '1'}</p>
            </div>
            <div style="text-align: right;">
              <span class="badge badge-ac">${room ? room.type : 'Standard'}</span>
              <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem;">Capacity: ${room ? room.capacity : 2} Occupant(s)</div>
            </div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
            <div style="background: var(--bg-primary); padding: 1.25rem; border-radius: var(--radius-md);">
              <h4 style="margin-bottom: 0.75rem; color: var(--primary);"><i class="fa-solid fa-id-card"></i> Resident Details</h4>
              <div style="font-size: 0.88rem; display: flex; flex-direction: column; gap: 0.4rem;">
                <div><strong>Name:</strong> ${student.name}</div>
                <div><strong>Student ID:</strong> ${student.id}</div>
                <div><strong>Department:</strong> ${student.dept} (${student.year})</div>
                <div><strong>Phone:</strong> ${student.phone || 'N/A'}</div>
                <div><strong>Emergency Contact:</strong> ${student.emergencyContact}</div>
              </div>
            </div>

            <div style="background: var(--bg-primary); padding: 1.25rem; border-radius: var(--radius-md);">
              <h4 style="margin-bottom: 0.75rem; color: var(--accent);"><i class="fa-solid fa-user-group"></i> Roommate Profile(s)</h4>
              ${roommates.length === 0 
                ? `<div style="font-size: 0.85rem; color: var(--text-muted);">No roommate assigned yet (Single occupancy / Vacant slot).</div>`
                : roommates.map(rm => `
                  <div style="padding: 0.5rem; background: var(--bg-secondary); border-radius: var(--radius-sm); margin-bottom: 0.5rem;">
                    <strong>${rm.name}</strong> (${rm.dept})
                    <div style="font-size: 0.75rem; color: var(--text-muted);">${rm.email} | ${rm.phone}</div>
                  </div>
                `).join('')}
            </div>
          </div>
        </div>
      `;
    }
  }

  renderStudentComplaintTracker() {
    const complaints = store.getComplaints().filter(c => c.studentId === store.getStudentId());
    const container = document.getElementById('student-complaint-history');
    if (!container) return;

    if (complaints.length === 0) {
      container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">No complaint tickets raised yet.</div>`;
      return;
    }

    container.innerHTML = complaints.map(c => `
      <div class="card" style="margin-bottom: 1rem; border-left: 4px solid var(--${c.status === 'Resolved' ? 'success' : c.status === 'In Progress' ? 'accent' : 'warning'});">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
          <div>
            <strong>Ticket ${c.id} - ${c.category}</strong>
            <div style="font-size: 0.75rem; color: var(--text-muted);">Logged on ${c.date}</div>
          </div>
          <span class="badge badge-${c.status === 'Resolved' ? 'resolved' : c.status === 'In Progress' ? 'progress' : 'pending'}">${c.status}</span>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.75rem;">${c.description}</p>
        ${c.remarks ? `
          <div style="background: var(--bg-primary); padding: 0.6rem; border-radius: var(--radius-sm); font-size: 0.8rem;">
            <strong style="color: var(--primary);"><i class="fa-solid fa-comment-dots"></i> Warden Response:</strong> ${c.remarks}
          </div>
        ` : ''}
      </div>
    `).join('');
  }

  async handleRaiseComplaint(event) {
    event.preventDefault();
    const form = document.getElementById('raise-complaint-form');
    this.clearFormErrors(form);

    const student = store.getStudent(store.getStudentId());
    if (!student) {
      this.showToast('No active student profile selected', 'danger');
      return;
    }

    const categoryInput = document.getElementById('cmp-category');
    const priorityInput = document.getElementById('cmp-priority');
    const descInput = document.getElementById('cmp-description');

    const category = categoryInput.value;
    const priority = priorityInput.value;
    const description = descInput.value;

    const cmpErr = Validator.validateComplaint(category, description);
    if (cmpErr) {
      if (cmpErr.field === 'category') this.showFieldError(categoryInput, cmpErr.message);
      if (cmpErr.field === 'description') this.showFieldError(descInput, cmpErr.message);
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    const res = await store.addComplaint({
      studentId: student.id,
      category,
      priority,
      description
    });

    if (res && res.success !== false) {
      form.reset();
      this.showToast('Complaint ticket raised successfully!', 'success');
      this.renderStudentComplaintTracker();
    } else {
      this.showToast(res?.message || 'Failed to submit complaint', 'danger');
    }
  }

  renderStudentLeaveTracker() {
    const leaveRequests = store.getLeaveRequests().filter(l => l.studentId === store.getStudentId());
    const container = document.getElementById('student-leave-history');
    if (!container) return;

    if (leaveRequests.length === 0) {
      container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 2rem;">No leave letters submitted yet.</div>`;
      return;
    }

    container.innerHTML = leaveRequests.map(r => `
      <div class="card" style="margin-bottom: 1rem;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
          <div>
            <strong>${r.leaveType}</strong>
            <div style="font-size: 0.75rem; color: var(--text-muted);">${r.startDate} to ${r.endDate}</div>
          </div>
          <span class="badge badge-${(r.status || 'pending').toLowerCase()}">${r.status}</span>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem;">${r.reason}</p>
        ${r.wardenNotes ? `
          <div style="background: var(--bg-primary); padding: 0.6rem; border-radius: var(--radius-sm); font-size: 0.8rem;">
            <strong style="color: var(--primary);"><i class="fa-solid fa-signature"></i> Warden Decision Notes:</strong> ${r.wardenNotes}
          </div>
        ` : ''}
      </div>
    `).join('');
  }

  async handleStudentLeave(event) {
    event.preventDefault();
    const form = document.getElementById('student-leave-form');
    this.clearFormErrors(form);

    const student = store.getStudent(store.getStudentId());
    if (!student) {
      this.showToast('No active student profile selected', 'danger');
      return;
    }

    const typeInput = document.getElementById('leave-type');
    const startInput = document.getElementById('leave-start');
    const endInput = document.getElementById('leave-end');
    const parentInput = document.getElementById('leave-parent');
    const reasonInput = document.getElementById('leave-reason');

    const leaveType = typeInput.value;
    const startDate = startInput.value;
    const endDate = endInput.value;
    const parentContact = parentInput.value;
    const reason = reasonInput.value;

    let hasError = false;

    const dateErr = Validator.validateLeaveDates(startDate, endDate);
    if (dateErr) {
      if (dateErr.field === 'start') this.showFieldError(startInput, dateErr.message);
      if (dateErr.field === 'end') this.showFieldError(endInput, dateErr.message);
      hasError = true;
    }

    const phoneErr = Validator.validatePhone(parentContact);
    if (phoneErr) {
      this.showFieldError(parentInput, phoneErr);
      hasError = true;
    }

    const reasonErr = Validator.validateRequired(reason, "Detailed reason");
    if (reasonErr) {
      this.showFieldError(reasonInput, reasonErr);
      hasError = true;
    } else if (reason.trim().length < 5) {
      this.showFieldError(reasonInput, "Reason must be at least 5 characters long.");
      hasError = true;
    }

    if (hasError) {
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    const cleanParent = parentContact.replace(/[\s-]/g, '');
    const res = await store.addLeaveRequest({
      studentId: student.id,
      leaveType,
      startDate,
      endDate,
      parentContact: cleanParent,
      reason
    });

    if (res && res.success !== false) {
      form.reset();
      this.showToast('Leave request letter sent to Warden!', 'success');
      this.renderStudentLeaveTracker();
    } else {
      this.showToast(res?.message || 'Failed to submit leave request', 'danger');
    }
  }

  renderStudentRules() {
    const search = document.getElementById('student-rule-search')?.value.toLowerCase() || '';
    let rules = store.getRules().filter(r => r.active);

    if (search) {
      rules = rules.filter(r => r.title.toLowerCase().includes(search) || r.description.toLowerCase().includes(search));
    }

    const container = document.getElementById('student-rules-list');
    if (!container) return;

    if (rules.length === 0) {
      container.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted);">No rules found matching search.</div>`;
      return;
    }

    container.innerHTML = rules.map((r, idx) => `
      <div class="card rule-item-card">
        <div class="rule-number">${idx + 1}</div>
        <div class="rule-content">
          <div style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.35rem;">
            <h4>${r.title}</h4>
            <span class="badge badge-ac">${r.category}</span>
          </div>
          <p>${r.description}</p>
        </div>
      </div>
    `).join('');
  }

  // Modals & Events
  setupEventListeners() {
    document.getElementById('role-select')?.addEventListener('change', async (e) => {
      await this.switchRole(e.target.value);
    });

    document.getElementById('student-select')?.addEventListener('change', async (e) => {
      store.setStudentId(e.target.value);
      this.showToast(`Switched active student view to ${e.target.value}`, 'info');
      await this.renderCurrentViewContent(this.currentView);
    });
  }

  async resetDemoData() {
    if (confirm('Reset all Hostel Management data back to default state?')) {
      store.logout();
      await store.refreshAllData();
      this.showToast('Demo data refreshed', 'success');
      this.showHomeLayout();
    }
  }

  toggleWardenModalPassword() {
    const input = document.getElementById('wrd-password');
    const icon = document.getElementById('wrd-password-eye-icon');
    if (!input || !icon) return;

    if (input.type === 'password') {
      input.type = 'text';
      icon.className = 'fa-solid fa-eye-slash';
    } else {
      input.type = 'password';
      icon.className = 'fa-solid fa-eye';
    }
  }

  async handleAddWarden(event) {
    event.preventDefault();
    const modal = document.getElementById('add-warden-modal');
    const form = modal ? modal.querySelector('form') : null;
    this.clearFormErrors(form);

    const nameInput = document.getElementById('wrd-name');
    const blockInput = document.getElementById('wrd-block');
    const emailInput = document.getElementById('wrd-email');
    const phoneInput = document.getElementById('wrd-phone');
    const passwordInput = document.getElementById('wrd-password');

    const name = nameInput.value;
    const block = blockInput.value;
    const email = emailInput.value;
    const phone = phoneInput.value;
    const password = passwordInput ? passwordInput.value : '';

    let hasError = false;

    const nameErr = Validator.validateName(name);
    if (nameErr) { this.showFieldError(nameInput, nameErr); hasError = true; }

    const emailErr = Validator.validateEmail(email);
    if (emailErr) { this.showFieldError(emailInput, emailErr); hasError = true; }

    const phoneErr = Validator.validatePhone(phone);
    if (phoneErr) { this.showFieldError(phoneInput, phoneErr); hasError = true; }

    const blockErr = Validator.validateRequired(block, "Hostel block assignment");
    if (blockErr) { this.showFieldError(blockInput, blockErr); hasError = true; }

    const passErr = Validator.validatePassword(password);
    if (passErr) { this.showFieldError(passwordInput, passErr); hasError = true; }

    if (hasError) {
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    const cleanPhone = phone.replace(/[\s-]/g, '');
    const cleanEmail = email.trim().toLowerCase();
    const res = await store.addWarden({ name, block, email: cleanEmail, phone: cleanPhone, password });
    if (res && res.success !== false) {
      localStorage.setItem('hms_last_warden_email', cleanEmail);
      localStorage.setItem('hms_last_warden_pass', password);
      sessionStorage.setItem('hms_last_warden_pass', password);
      localStorage.setItem('hms_last_warden_name', name);
      localStorage.setItem('hms_last_warden_block', block);

      this.closeModal('add-warden-modal');
      this.showToast(`New Warden Officer ${name} registered successfully with custom password!`, 'success');
      if (form) form.reset();
      this.renderAdminWardens();
    } else {
      this.showToast(res?.message || 'Failed to add warden officer', 'danger');
      if (res?.message && res.message.toLowerCase().includes('email')) {
        this.showFieldError(emailInput, res.message);
      }
    }
  }

  async removeWarden(wardenId, wardenName) {
    if (!confirm(`Are you sure you want to remove Warden Officer ${wardenName} (${wardenId})?\n\nThis will permanently delete their officer account and credentials from MySQL.`)) {
      return;
    }

    try {
      const res = await store.deleteWarden(wardenId);
      if (res && res.success !== false) {
        this.showToast(`Warden ${wardenName} (${wardenId}) was removed successfully.`, 'success');
        await store.fetchWardens();
        await store.fetchMetrics();
        this.renderAdminWardens();
        this.renderAdminOverview();
      } else {
        this.showToast(res?.message || 'Failed to remove warden officer', 'danger');
      }
    } catch (err) {
      this.showToast(err.message || 'Error occurred while removing warden', 'danger');
    }
  }

  async handleAddRule(event) {
    event.preventDefault();
    const modal = document.getElementById('add-rule-modal');
    const form = modal ? modal.querySelector('form') : null;
    this.clearFormErrors(form);

    const titleInput = document.getElementById('rule-title');
    const catInput = document.getElementById('rule-category');
    const descInput = document.getElementById('rule-description');

    const title = titleInput.value;
    const category = catInput.value;
    const description = descInput.value;

    const ruleErr = Validator.validateRule(title, description);
    if (ruleErr) {
      if (ruleErr.field === 'title') this.showFieldError(titleInput, ruleErr.message);
      if (ruleErr.field === 'description') this.showFieldError(descInput, ruleErr.message);
      this.showToast("Please correct highlighted errors before submitting.", "danger");
      return;
    }

    const res = await store.addRule({ title, category, description, active: true });
    if (res && res.success !== false) {
      this.closeModal('add-rule-modal');
      this.showToast('Hostel rule added!', 'success');
      this.renderAdminRules();
      this.renderPublicRules();
    } else {
      this.showToast(res?.message || 'Failed to add hostel rule', 'danger');
    }
  }

  // =========================================================================
  // GATE SCANNING & HOSTEL QR CONTROLLER METHODS
  // =========================================================================
  copyQRToken(elementId) {
    const textEl = document.getElementById(elementId);
    if (!textEl) return;
    const token = textEl.innerText.trim();
    navigator.clipboard.writeText(token).then(() => {
      this.showToast("Hostel QR Token copied to clipboard!", "success");
    }).catch(() => {
      this.showToast(`Token: ${token}`, "info");
    });
  }

  async renderAdminQRView() {
    const token = await store.fetchCommonQRCodeToken();
    const txtEl = document.getElementById('admin-qr-token-text');
    const imgEl = document.getElementById('admin-qr-image');
    if (txtEl) txtEl.innerText = token;
    if (imgEl) imgEl.src = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(token)}`;
  }

  async renderWardenQRView() {
    const token = await store.fetchCommonQRCodeToken();
    const txtEl = document.getElementById('warden-qr-token-text');
    const imgEl = document.getElementById('warden-qr-image');
    if (txtEl) txtEl.innerText = token;
    if (imgEl) imgEl.src = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(token)}`;
  }







  async renderAdminGateView() {
    await this.renderAdminQRView();
    const student = document.getElementById('admin-gate-search')?.value?.trim();
    const type = document.getElementById('admin-gate-type-filter')?.value;
    const logs = await store.fetchAdminGateLogs(student, null, null, type);

    const tbody = document.getElementById('admin-gate-table-body');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No campus gate movements recorded yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = logs.map(l => {
      const isIN = (l.logType || l.type) === 'IN';
      return `
        <tr>
          <td><strong>${l.studentName || l.studentId}</strong> <div style="font-size: 0.75rem; color: var(--text-muted);">${l.studentId}</div></td>
          <td>${l.room || 'General'}</td>
          <td>
            <span class="badge ${isIN ? 'badge-approved' : 'badge-pending'}">
              <i class="fa-solid fa-${isIN ? 'arrow-right-to-bracket' : 'arrow-right-from-bracket'}"></i> Gate ${l.logType || l.type}
            </span>
          </td>
          <td>${l.timestamp ? l.timestamp.replace('T', ' ') : 'N/A'}</td>
          <td><span style="color: var(--success);"><i class="fa-solid fa-shield-check"></i> Verified in MySQL</span></td>
        </tr>
      `;
    }).join('');
  }

  async renderWardenGateView() {
    await this.renderWardenQRView();
    const student = document.getElementById('warden-gate-search')?.value?.trim();
    const type = document.getElementById('warden-gate-type-filter')?.value;
    const logs = await store.fetchWardenGateLogs(student, null, null, type);

    const tbody = document.getElementById('warden-gate-table-body');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No gate movements recorded for this block.</td></tr>`;
      return;
    }

    tbody.innerHTML = logs.map(l => {
      const isIN = (l.logType || l.type) === 'IN';
      return `
        <tr>
          <td><strong>${l.studentName || l.studentId}</strong> <div style="font-size: 0.75rem; color: var(--text-muted);">${l.studentId}</div></td>
          <td>${l.room || 'Assigned Block'}</td>
          <td>
            <span class="badge ${isIN ? 'badge-approved' : 'badge-pending'}">
              <i class="fa-solid fa-${isIN ? 'arrow-right-to-bracket' : 'arrow-right-from-bracket'}"></i> Gate ${l.logType || l.type}
            </span>
          </td>
          <td>${l.timestamp ? l.timestamp.replace('T', ' ') : 'N/A'}</td>
          <td><span style="color: var(--success);"><i class="fa-solid fa-check"></i> Warden Audited</span></td>
        </tr>
      `;
    }).join('');
  }

  async renderStudentGateView() {
    const logs = await store.fetchMyGateLogs();
    const tbody = document.getElementById('student-gate-table-body');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No gate entry/exit logs recorded yet. Use the scan button to record entry/exit.</td></tr>`;
      return;
    }

    tbody.innerHTML = logs.map(l => {
      const isIN = (l.logType || l.type) === 'IN';
      return `
        <tr>
          <td>
            <span class="badge ${isIN ? 'badge-approved' : 'badge-pending'}">
              <i class="fa-solid fa-${isIN ? 'arrow-right-to-bracket' : 'arrow-right-from-bracket'}"></i> Gate ${l.logType || l.type}
            </span>
          </td>
          <td>${l.timestamp ? l.timestamp.replace('T', ' ') : 'Just Now'}</td>
          <td>Room ${l.room || 'Resident'}</td>
          <td><span style="color: var(--success);"><i class="fa-solid fa-circle-check"></i> Recorded in MySQL</span></td>
        </tr>
      `;
    }).join('');
  }

  async syncDatabase() {
    this.showToast('Synchronizing with MySQL database...', 'info');
    await store.refreshAllData();
    if (this.currentView) {
      await this.renderCurrentViewContent(this.currentView);
    }
    this.showToast('All database records synchronized!', 'success');
  }

  async simulateGateScan() {
    await this.handleScanQR("HOSTEL-HUB-COMMON-GATE-QR-2026");
  }

  async handleScanQR(qrToken) {
    const banner = document.getElementById('student-scan-result-banner');
    if (banner) banner.style.display = 'none';

    const res = await store.scanGateQR(qrToken);

    if (res && res.success !== false) {
      const typeStr = res.entryType || res.type || 'IN';
      const timeStr = res.scanTime || '';
      const resultText = res.resultMsg || res.message || `${typeStr} recorded successfully at ${timeStr}`;

      if (banner) {
        const isIN = typeStr === 'IN';
        banner.className = `alert-banner ${isIN ? 'alert-success' : 'alert-warning'}`;
        banner.style.background = isIN ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)';
        banner.style.border = `1px solid ${isIN ? '#10b981' : '#f59e0b'}`;
        banner.style.color = isIN ? '#10b981' : '#f59e0b';
        banner.innerHTML = `
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <i class="fa-solid fa-${isIN ? 'circle-check' : 'circle-exclamation'}" style="font-size: 1.5rem;"></i>
            <div>
              <strong style="font-size: 1.05rem;">${resultText}</strong>
              <div style="font-size: 0.8rem; opacity: 0.9; margin-top: 0.2rem;">
                Hostel Notification Sent to Warden & Registered Parent
              </div>
            </div>
          </div>
        `;
        banner.style.display = 'block';
      }

      this.showToast(resultText, 'success');
      await this.renderStudentGateView();
    } else {
      const errMsg = res?.message || 'Invalid or unrecognized Hostel QR Code scan.';
      if (banner) {
        banner.className = 'alert-banner alert-danger';
        banner.style.background = 'rgba(239, 68, 68, 0.15)';
        banner.style.border = '1px solid #ef4444';
        banner.style.color = '#ef4444';
        banner.innerHTML = `
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <i class="fa-solid fa-triangle-exclamation" style="font-size: 1.5rem;"></i>
            <div>
              <strong>Scan Failed</strong>
              <div style="font-size: 0.85rem; margin-top: 0.2rem;">${errMsg}</div>
            </div>
          </div>
        `;
        banner.style.display = 'block';
      }
      this.showToast(errMsg, 'danger');
    }
  }

  openModal(modalId) {
    document.getElementById(modalId)?.classList.add('active');
  }

  closeModal(modalId) {
    document.getElementById(modalId)?.classList.remove('active');
  }

  showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <i class="fa-solid fa-${type === 'success' ? 'circle-check' : type === 'danger' ? 'circle-xmark' : 'circle-info'}"></i>
      <span>${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }
}

let app;
window.addEventListener('DOMContentLoaded', () => {
  app = new AppController();
});
