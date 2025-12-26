// API 基础地址
var API_BASE = 'http://localhost:8080/api';

// 获取存储的token
function getToken() {
    return localStorage.getItem('token');
}

// 获取当前用户
function getUser() {
    var userStr = localStorage.getItem('user');
    if (userStr) {
        return JSON.parse(userStr);
    }
    return null;
}

// 保存登录信息
function saveLogin(token, user) {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
}

// 退出登录
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

// 检查登录状态
function checkLogin() {
    var user = getUser();
    if (!user) {
        window.location.href = 'index.html';
        return null;
    }
    return user;
}

// 根据角色跳转
function redirectByRole(role) {
    if (role === 'MANAGER') {
        window.location.href = 'manager.html';
    } else if (role === 'BARISTA') {
        window.location.href = 'barista.html';
    } else {
        window.location.href = 'customer.html';
    }
}

// 发送请求
function request(url, method, data, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, API_BASE + url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    var token = getToken();
    if (token) {
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    }

    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            var response;
            try {
                response = JSON.parse(xhr.responseText);
            } catch (e) {
                response = { code: 500, message: '请求失败' };
            }
            callback(response);
        }
    };

    if (data) {
        xhr.send(JSON.stringify(data));
    } else {
        xhr.send();
    }
}

// GET 请求
function get(url, callback) {
    request(url, 'GET', null, callback);
}

// POST 请求
function post(url, data, callback) {
    request(url, 'POST', data, callback);
}

// PUT 请求
function put(url, data, callback) {
    request(url, 'PUT', data, callback);
}

// DELETE 请求
function del(url, callback) {
    request(url, 'DELETE', null, callback);
}

// 显示提示
function showAlert(message, type) {
    var alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-' + (type || 'warning');
    alertDiv.textContent = message;
    alertDiv.style.position = 'fixed';
    alertDiv.style.top = '20px';
    alertDiv.style.left = '50%';
    alertDiv.style.transform = 'translateX(-50%)';
    alertDiv.style.zIndex = '9999';
    document.body.appendChild(alertDiv);

    setTimeout(function() {
        alertDiv.remove();
    }, 3000);
}

// 格式化时间
function formatTime(timeStr) {
    if (!timeStr) return '-';
    var date = new Date(timeStr);
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    return month + '/' + day + ' ' + hour + ':' + (minute < 10 ? '0' : '') + minute;
}

// 获取状态名称
function getStatusName(status) {
    var names = {
        'PENDING': '待处理',
        'MAKING': '制作中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
    };
    return names[status] || status;
}

// 获取状态样式类
function getStatusClass(status) {
    return 'status status-' + status.toLowerCase();
}
