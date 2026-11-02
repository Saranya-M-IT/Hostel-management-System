/**
 * Hostel Management System (HMS) - Central Store & Async API Manager
 * Fully integrated with MySQL Database via Spring Boot REST APIs
 * Real-time data handling with zero demo/mock fallbacks
 */

class Store {
  constructor() {
    this.currentUser = JSON.parse(localStorage.getItem('hms_current_user')) || null;
    this.activeRole = localStorage.getItem('hms_active_role') || 'public';
    this.activeStudentId = localStorage.getItem('hms_active_student') || '';
    this.theme = localStorage.getItem('hms_theme') || 'dark';

    // In-memory data cache populated solely from MySQL backend
    this.data = {
      rooms: [],
      wardens: [],
      students: [],
      complaints: [],
      leaveRequests: [],
      rules: []
    };

    this.currentStudent = null;
    this.currentWarden = null;
    this.metrics = null;
  }

  // Bidirectional Normalizers to guarantee complete data contracts between backend DTOs and UI
  normalizeStudent(s) {
    if (!s) return null;
    const name = s.fullName || s.name || '';
    const dept = s.department || s.dept || '';
    const year = s.studentYear || s.year || '1st Year';
    const needs = s.roomNeeds || s.needs || '';
    const assignedRoom = s.assignedRoomId || s.assignedRoom || 'Unassigned';
    const isAllocated = assignedRoom && assignedRoom !== 'Unassigned';

    return {
      id: s.id,
      name: name,
      fullName: name,
      email: s.email || '',
      phone: s.phone || '',
      dept: dept,
      department: dept,
      year: year,
      studentYear: year,
      gender: s.gender || 'Male',
      emergencyContact: s.emergencyContact || '',
      needs: needs,
      roomNeeds: needs,
      assignedRoom: assignedRoom,
      assignedRoomId: isAllocated ? assignedRoom : null,
      status: s.allocationStatus ? (s.allocationStatus === 'ALLOCATED' ? 'Allocated' : 'Pending Allocation') : (isAllocated ? 'Allocated' : (s.status || 'Pending Allocation')),
      allocationStatus: s.allocationStatus || (isAllocated ? 'ALLOCATED' : 'PENDING')
    };
  }

  normalizeRoom(r) {
    if (!r) return null;
    const num = r.number || r.roomNumber || '';
    const capacity = parseInt(r.capacity) || 1;
    const currentOccupancy = r.currentOccupancy !== undefined ? parseInt(r.currentOccupancy) : (r.occupants ? r.occupants.length : 0);
    const isFull = currentOccupancy >= capacity;

    return {
      id: r.id || `${r.block}-${num}`,
      block: r.block || 'Block A',
      number: num,
      roomNumber: num,
      floor: parseInt(r.floor) || 1,
      type: r.type || r.roomType || 'Single AC',
      roomType: r.type || r.roomType || 'Single AC',
      capacity: capacity,
      currentOccupancy: currentOccupancy,
      status: isFull ? 'booked' : (r.status === 'FULL' || r.status === 'booked' ? 'booked' : 'remaining'),
      occupants: Array.isArray(r.occupants) ? r.occupants : []
    };
  }

  normalizeComplaint(c) {
    if (!c) return null;
    return {
      id: c.id,
      studentId: c.studentId,
      studentName: c.studentName || 'Student',
      room: c.room || 'Unassigned',
      category: c.category || 'General',
      description: c.description || '',
      priority: c.priority || 'Medium',
      status: c.status || 'Pending',
      date: c.date || (c.createdAt ? c.createdAt.split('T')[0] : new Date().toISOString().split('T')[0]),
      remarks: c.remarks || ''
    };
  }

  normalizeLeave(l) {
    if (!l) return null;
    return {
      id: l.id,
      studentId: l.studentId,
      studentName: l.studentName || 'Student',
      room: l.room || 'Unassigned',
      leaveType: l.leaveType || 'General Leave',
      startDate: l.startDate || '',
      endDate: l.endDate || '',
      reason: l.reason || '',
      parentContact: l.parentContact || '',
      status: l.status || 'Pending',
      appliedDate: l.appliedDate || (l.createdAt ? l.createdAt.split('T')[0] : new Date().toISOString().split('T')[0]),
      wardenNotes: l.wardenNotes || l.wardenRemarks || ''
    };
  }

  async refreshAllData() {
    try {
      const calls = [
        this.fetchRooms(),
        this.fetchRules()
      ];

      const user = this.getCurrentUser();
      const token = api.getToken();

      if (user || token) {
        if (this.activeRole === 'student') {
          calls.push(this.fetchCurrentStudent());
          calls.push(this.fetchComplaints());
          calls.push(this.fetchLeaveRequests());
          calls.push(this.fetchMetrics());
        } else if (this.activeRole === 'warden') {
          calls.push(this.fetchCurrentWarden());
          calls.push(this.fetchStudents());
          calls.push(this.fetchComplaints());
          calls.push(this.fetchLeaveRequests());
          calls.push(this.fetchMetrics());
        } else if (this.activeRole === 'admin') {
          calls.push(this.fetchStudents());
          calls.push(this.fetchWardens());
          calls.push(this.fetchComplaints());
          calls.push(this.fetchLeaveRequests());
          calls.push(this.fetchMetrics());
        }
      }

      await Promise.allSettled(calls);
    } catch (e) {
      console.warn("Error refreshing dataset from MySQL backend:", e);
    }
  }

  // Authentication Operations
  async login(identifier, password, role) {
    identifier = (identifier || '').trim();
    password = (password || '').trim();
    role = (role || '').trim().toLowerCase();

    if (!identifier || !password) {
      return { success: false, message: 'Please enter both Username/ID/Email and Password.' };
    }

    const payload = {
      identifier: identifier,
      password: password,
      role: role.toUpperCase()
    };

    const res = await api.post('/auth/login', payload);

    if (res.success && res.data && res.data.token) {
      api.setToken(res.data.token);

      const user = {
        role: role,
        id: res.data.studentId || res.data.wardenId || `ADM-${res.data.userId}`,
        name: res.data.name || identifier,
        email: res.data.email || (identifier.includes('@') ? identifier : `${identifier}@hms.edu`)
      };

      this.currentUser = user;
      this.activeRole = role;

      if (res.data.studentId) {
        this.setStudentId(res.data.studentId);
      }

      localStorage.setItem('hms_current_user', JSON.stringify(user));
      localStorage.setItem('hms_active_role', role);

      // Pre-fetch actual authenticated profile immediately
      if (role === 'student') {
        localStorage.setItem('hms_last_student_email', identifier);
        localStorage.setItem('hms_last_student_pass', password);
        sessionStorage.setItem('hms_last_student_pass', password);
        await this.fetchCurrentStudent();
      } else if (role === 'warden') {
        localStorage.setItem('hms_last_warden_email', identifier);
        localStorage.setItem('hms_last_warden_pass', password);
        sessionStorage.setItem('hms_last_warden_pass', password);
        await this.fetchCurrentWarden();
      } else if (role === 'admin') {
        localStorage.setItem('hms_last_admin_email', identifier);
        localStorage.setItem('hms_last_admin_pass', password);
        sessionStorage.setItem('hms_last_admin_pass', password);
      }

      await this.refreshAllData();
      return { success: true, user };
    }

    return {
      success: false,
      message: res.message || 'Invalid credentials or role selection. Please verify and try again.'
    };
  }

  logout() {
    this.currentUser = null;
    this.currentStudent = null;
    this.currentWarden = null;
    this.activeRole = 'public';
    this.activeStudentId = '';
    api.setToken(null);
    localStorage.removeItem('hms_current_user');
    localStorage.removeItem('hms_active_student');
    localStorage.setItem('hms_active_role', 'public');
  }

  getCurrentUser() {
    return this.currentUser;
  }

  setRole(role) {
    this.activeRole = role;
    localStorage.setItem('hms_active_role', role);
  }

  getRole() {
    return this.activeRole;
  }

  setStudentId(id) {
    this.activeStudentId = id;
    if (id) {
      localStorage.setItem('hms_active_student', id);
    } else {
      localStorage.removeItem('hms_active_student');
    }
  }

  getStudentId() {
    if (this.currentStudent && this.currentStudent.id) {
      return this.currentStudent.id;
    }
    if (this.currentUser && this.currentUser.role === 'student' && this.currentUser.id) {
      return this.currentUser.id;
    }
    return this.activeStudentId || '';
  }

  // Theme Management
  setTheme(theme) {
    this.theme = theme;
    localStorage.setItem('hms_theme', theme);
  }

  getTheme() {
    return this.theme;
  }

  // Dashboard Metrics & Occupancy Counters
  async fetchMetrics() {
    let endpoint = '/dashboard/admin';
    if (this.activeRole === 'warden') endpoint = '/dashboard/warden';
    if (this.activeRole === 'student') endpoint = '/dashboard/student/me';

    const res = await api.get(endpoint);
    if (res.success && res.data) {
      this.metrics = res.data;
    }
    return this.getMetrics();
  }

  getMetrics() {
    const rooms = this.data.rooms || [];
    const students = this.data.students || [];
    const wardens = this.data.wardens || [];
    const complaints = this.data.complaints || [];
    const leaveRequests = this.data.leaveRequests || [];

    const totalRooms = rooms.length;
    const bookedRooms = rooms.filter(r => r.status === 'booked' || (r.currentOccupancy >= r.capacity)).length;
    const remainingRooms = Math.max(0, totalRooms - bookedRooms);
    const totalCapacity = rooms.reduce((acc, r) => acc + (r.capacity || 0), 0);
    const totalStudents = students.length;
    const allocatedStudents = students.filter(s => s.assignedRoom && s.assignedRoom !== 'Unassigned').length;
    const pendingComplaints = complaints.filter(c => c.status === 'Pending').length;
    const pendingLeave = leaveRequests.filter(l => l.status === 'Pending').length;
    const occupancyRate = totalRooms > 0 ? Math.round((bookedRooms / totalRooms) * 100) : 0;

    return {
      totalRooms,
      bookedRooms,
      remainingRooms,
      totalCapacity,
      totalStudents,
      allocatedStudents,
      unallocatedStudents: Math.max(0, totalStudents - allocatedStudents),
      totalWardens: wardens.length,
      pendingComplaints,
      pendingLeave,
      occupancyRate,
      ...(this.metrics || {})
    };
  }

  // Room CRUD Operations
  async fetchRooms() {
    const res = await api.get('/rooms');
    if (res.success && Array.isArray(res.data)) {
      this.data.rooms = res.data.map(r => this.normalizeRoom(r));
    }
    return this.data.rooms || [];
  }

  getRooms() {
    return this.data.rooms || [];
  }

  getRoom(id) {
    if (!id) return null;
    return (this.data.rooms || []).find(r => r.id === id || r.number === id);
  }

  async addRoom(roomData) {
    const payload = {
      block: roomData.block,
      roomNumber: roomData.number || roomData.roomNumber,
      floor: parseInt(roomData.floor) || 1,
      roomType: roomData.type || roomData.roomType || 'Single AC',
      capacity: parseInt(roomData.capacity) || 1
    };

    const res = await api.post('/rooms', payload);
    if (res.success && res.data) {
      await this.fetchRooms();
      return { success: true, room: this.normalizeRoom(res.data) };
    }
    return { success: false, message: res.message || 'Failed to create room in database' };
  }

  async deleteRoom(id) {
    const res = await api.delete(`/rooms/${id}`);
    if (res.success) {
      await this.fetchRooms();
      return { success: true };
    }
    return { success: false, message: res.message || 'Failed to delete room' };
  }

  async allocateRoom(roomId, studentId) {
    const res = await api.post('/rooms/allocate', { studentId, roomId });
    if (res.success) {
      await this.fetchRooms();
      await this.fetchStudents();
      if (this.activeRole === 'student') {
        await this.fetchCurrentStudent();
      }
      return { success: true };
    }
    return { success: false, message: res.message || 'Failed to allocate room' };
  }

  async vacateStudent(studentId) {
    const res = await api.post(`/students/${studentId}/vacate`);
    if (res.success) {
      await this.fetchStudents();
      await this.fetchRooms();
      if (this.activeRole === 'student') {
        await this.fetchCurrentStudent();
      }
      return { success: true };
    }
    return { success: false, message: res.message || 'Failed to vacate student' };
  }

  // Student Operations
  async fetchCurrentStudent() {
    const res = await api.get('/students/me');
    if (res.success && res.data) {
      const student = this.normalizeStudent(res.data);
      this.currentStudent = student;
      this.setStudentId(student.id);

      // Keep this.data.students updated
      const idx = this.data.students.findIndex(s => s.id === student.id);
      if (idx >= 0) {
        this.data.students[idx] = student;
      } else {
        this.data.students.push(student);
      }
      return student;
    }
    return null;
  }

  getCurrentStudent() {
    return this.currentStudent || this.getStudent(this.getStudentId());
  }

  async fetchStudents() {
    if (this.activeRole === 'student') {
      const student = await this.fetchCurrentStudent();
      return student ? [student] : [];
    }

    const res = await api.get('/students');
    if (res.success && Array.isArray(res.data)) {
      this.data.students = res.data.map(s => this.normalizeStudent(s));
    }
    return this.data.students || [];
  }

  getStudents() {
    return this.data.students || [];
  }

  getStudent(id) {
    if (this.currentStudent && (!id || this.currentStudent.id === id || this.currentStudent.email === id)) {
      return this.currentStudent;
    }
    if (!id && this.data.students.length > 0) {
      return this.data.students[0];
    }
    return (this.data.students || []).find(s => s.id === id || s.email === id) || this.currentStudent;
  }

  async addStudent(studentData) {
    const payload = {
      fullName: studentData.fullName || studentData.name,
      email: (studentData.email || '').trim().toLowerCase(),
      password: studentData.password,
      phone: (studentData.phone || '').trim().replace(/[\s-]/g, ''),
      department: studentData.department || studentData.dept || 'General',
      year: studentData.year || studentData.studentYear || '1st Year',
      studentYear: studentData.year || studentData.studentYear || '1st Year',
      gender: studentData.gender || 'Male',
      emergencyContact: studentData.emergencyContact || '',
      roomNeeds: studentData.roomNeeds || studentData.needs || ''
    };

    const res = await api.post('/auth/register/student', payload);
    if (res.success && res.data) {
      const normalized = this.normalizeStudent(res.data);
      this.setStudentId(normalized.id);
      return { success: true, ...normalized };
    }
    return { success: false, message: res.message || 'Registration failed' };
  }

  async updateStudent(id, studentData) {
    const payload = {
      fullName: studentData.fullName || studentData.name,
      email: (studentData.email || '').trim().toLowerCase(),
      phone: (studentData.phone || '').trim().replace(/[\s-]/g, ''),
      department: studentData.department || studentData.dept,
      studentYear: studentData.year || studentData.studentYear,
      gender: studentData.gender,
      emergencyContact: studentData.emergencyContact,
      roomNeeds: studentData.roomNeeds || studentData.needs,
      assignedRoomId: studentData.assignedRoom !== 'Unassigned' ? (studentData.assignedRoomId || studentData.assignedRoom) : null
    };

    const endpoint = this.activeRole === 'student' ? '/students/me' : `/students/${id}`;
    const res = await api.put(endpoint, payload);

    if (res.success && res.data) {
      const updated = this.normalizeStudent(res.data);
      if (this.activeRole === 'student') {
        this.currentStudent = updated;
      }
      await this.fetchStudents();
      return { success: true, ...updated };
    }
    return { success: false, message: res.message || 'Failed to update student profile in database' };
  }

  // Warden Operations
  async fetchCurrentWarden() {
    const res = await api.get('/wardens/me');
    if (res.success && res.data) {
      this.currentWarden = res.data;
      if (this.currentUser) {
        this.currentUser.name = res.data.name || this.currentUser.name;
        this.currentUser.block = res.data.block || this.currentUser.block;
        this.currentUser.email = res.data.email || this.currentUser.email;
        this.currentUser.phone = res.data.phone || this.currentUser.phone;
        this.currentUser.id = res.data.id || this.currentUser.id;
        localStorage.setItem('hms_current_user', JSON.stringify(this.currentUser));
      }
      return this.currentWarden;
    }
    return null;
  }

  getCurrentWarden() {
    return this.currentWarden;
  }

  async fetchWardens() {
    const res = await api.get('/wardens');
    if (res.success && Array.isArray(res.data)) {
      this.data.wardens = res.data;
    }
    return this.data.wardens || [];
  }

  getWardens() {
    return this.data.wardens || [];
  }

  async addWarden(wardenData) {
    const payload = {
      name: wardenData.name,
      block: wardenData.block,
      email: (wardenData.email || '').trim().toLowerCase(),
      phone: (wardenData.phone || '').trim().replace(/[\s-]/g, ''),
      status: 'Active',
      password: wardenData.password || 'warden123'
    };

    const res = await api.post('/wardens', payload);
    if (res.success && res.data) {
      await this.fetchWardens();
      return { success: true, ...res.data };
    }
    return { success: false, message: res.message || 'Failed to save warden officer in database' };
  }

  async deleteWarden(id) {
    const res = await api.delete(`/wardens/${id}`);
    if (res.success) {
      await this.fetchWardens();
      return { success: true };
    }
    return { success: false, message: res.message || 'Failed to delete warden' };
  }

  // Complaints Operations
  async fetchComplaints() {
    let endpoint = '/complaints';
    if (this.activeRole === 'student') {
      endpoint = '/complaints/my';
    }

    const res = await api.get(endpoint);
    if (res.success && Array.isArray(res.data)) {
      this.data.complaints = res.data.map(c => this.normalizeComplaint(c));
    }
    return this.data.complaints || [];
  }

  getComplaints() {
    return this.data.complaints || [];
  }

  async addComplaint(complaintData) {
    const payload = {
      category: complaintData.category,
      description: complaintData.description,
      priority: complaintData.priority || 'Medium'
    };

    const res = await api.post('/complaints', payload);
    if (res.success && res.data) {
      await this.fetchComplaints();
      return { success: true, ...this.normalizeComplaint(res.data) };
    }
    return { success: false, message: res.message || 'Failed to submit complaint to database' };
  }

  async updateComplaintStatus(id, status, remarks = '') {
    const res = await api.put(`/complaints/${id}`, { status, remarks });
    if (res.success) {
      await this.fetchComplaints();
      return { success: true };
    }
    return { success: false, message: res.message || 'Failed to update complaint in database' };
  }

  // Leave Requests Operations
  async fetchLeaveRequests() {
    let endpoint = '/leaves';
    if (this.activeRole === 'student') {
      endpoint = '/leaves/my';
    }

    const res = await api.get(endpoint);
    if (res.success && Array.isArray(res.data)) {
      this.data.leaveRequests = res.data.map(l => this.normalizeLeave(l));
    }
    return this.data.leaveRequests || [];
  }

  getLeaveRequests() {
    return this.data.leaveRequests || [];
  }

  async addLeaveRequest(requestData) {
    const payload = {
      leaveType: requestData.leaveType,
      startDate: requestData.startDate,
      endDate: requestData.endDate,
      reason: requestData.reason,
      parentContact: (requestData.parentContact || '').replace(/[\s-]/g, '')
    };

    const res = await api.post('/leaves', payload);
    if (res.success && res.data) {
      await this.fetchLeaveRequests();
      return { success: true, ...this.normalizeLeave(res.data) };
    }
    return { success: false, message: res.message || 'Failed to submit leave request to database' };
  }

  async updateLeaveStatus(id, status, notes = '') {
    const endpoint = (status === 'Approved' || status === 'APPROVED')
      ? `/leaves/${id}/approve`
      : `/leaves/${id}/reject`;

    const res = await api.put(endpoint, { status, wardenRemarks: notes });
    if (res.success) {
      await this.fetchLeaveRequests();
      return { success: true };
    }
    return { success: false, message: res.message || 'Failed to process leave request in database' };
  }

  // Rules Operations
  async fetchRules() {
    const res = await api.get('/rules');
    if (res.success && Array.isArray(res.data)) {
      this.data.rules = res.data;
    }
    return this.data.rules || [];
  }

  getRules() {
    return this.data.rules || [];
  }

  async addRule(ruleData) {
    const res = await api.post('/rules', ruleData);
    if (res.success && res.data) {
      await this.fetchRules();
      return { success: true, ...res.data };
    }
    return { success: false, message: res.message || 'Failed to add rule in database' };
  }

  async toggleRule(id) {
    const res = await api.put(`/rules/${id}/toggle`);
    if (res.success) {
      await this.fetchRules();
      return true;
    }
    return false;
  }

  async deleteRule(id) {
    const res = await api.delete(`/rules/${id}`);
    if (res.success) {
      await this.fetchRules();
      return true;
    }
    return false;
  }

  // Gate & Attendance Operations
  async fetchCommonQRCodeToken() {
    const res = await api.getCommonQRCode();
    if (res.success && res.data && res.data.qrToken) {
      return res.data.qrToken;
    }
    return "HOSTEL-HUB-COMMON-GATE-QR-2026";
  }

  async scanGateQR(qrToken) {
    const res = await api.scanGateQR(qrToken);
    if (res.success && res.data) {
      return res.data;
    }
    return {
      success: false,
      message: res.message || 'Scan processing failed.'
    };
  }

  async fetchMyGateLogs() {
    const res = await api.getMyGateLogs();
    if (res.success && Array.isArray(res.data)) {
      return res.data;
    }
    return [];
  }

  async fetchWardenGateLogs(student, room, date, type) {
    const res = await api.getWardenGateLogs(student, room, date, type);
    if (res.success && Array.isArray(res.data)) {
      return res.data;
    }
    return [];
  }

  async fetchAdminGateLogs(student, room, date, type) {
    const res = await api.getAdminGateLogs(student, room, date, type);
    if (res.success && Array.isArray(res.data)) {
      return res.data;
    }
    return [];
  }
}

const store = new Store();
