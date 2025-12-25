let stompClient = null;
let notifications = [];
let taskList = $('#tasksList');
$(document).ready(function () {
    loadTasks();
    connectWebSocket();

    $('#taskForm').submit(function (e) {
        e.preventDefault();
        saveTask();
    });

    $('#cancelEditBtn').click(function () {
        resetForm();
    });
});

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        updateConnectionStatus(true, 'Connected to WebSocket');
        stompClient.subscribe('/topic/notifications', function (message) {
            const notification = JSON.parse(message.body);
            addNotification(notification);
        });
    }, function () {
        updateConnectionStatus(false, 'WebSocket disconnected');
        // setTimeout(connectWebSocket, 5000);
    });
}

function updateConnectionStatus(connected, message) {
    const statusElement = $('#socketStatus');
    const textElement = $('#connectionStatus');

    if (connected) {
        statusElement.removeClass('disconnected').addClass('connected');
        textElement.text(message);
    } else {
        statusElement.removeClass('connected').addClass('disconnected');
        textElement.text(message);
    }
}

function addNotification(notification) {
    notifications.unshift(notification);
    if (notifications.length > 30) notifications.pop();

    const html = notifications.map(notif => `
                <div class="notification-item">
                    <strong>${notif.taskTitle}</strong><br>
                    <small>${notif.message} - ${notif.status}</small><br>
                    <small class="text-muted">${new Date(notif.timestamp).toLocaleString()}</small>
                </div>
            `).join('');

    $('#notifications').html(html);
}

function clearNotifications() {
    notifications = [];
    $('#notifications').html('<div class="notification-item"><small>Notifications cleared</small></div>');
}

function loadTasks() {
    $.ajax({
        url: '/api/tasks',
        method: 'GET',
        success: function (tasks) {
            displayTasks(tasks);
        },
        error: function (xhr) {
            showAlert('Error loading tasks: ' + xhr.responseText, 'danger');
        }
    });
}

function filterTasks(status) {
    const url = status ? `/api/tasks/status/${status}` : '/api/tasks';
    $.ajax({
        url: url,
        method: 'GET',
        success: displayTasks,
        error: function (xhr) {
            showAlert('Error filtering tasks: ' + xhr.responseText, 'danger');
        }
    });
}

function displayTasks(tasks) {
    if (tasks.length === 0) {
        taskList.html('<div class="alert alert-info">No tasks found. Create one!</div>');
        return;
    }

    const map = tasks.map(task => {
        let html = '';
        html += `<div class="card task-card">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h5 class="card-title">${task.title}</h5>
                                <p class="card-text">${task.description || 'No description'}</p>
                                <span class="badge ${getStatusClass(task.status)} status-badge">
                                    ${task.status}
                                </span>
                                <small class="text-muted ms-2">
                                    Created: ${new Date(task.createdAt).toLocaleString()}
                                </small>`;
        if (task.updatedAt !== null) {
            html += ` <small class="text-muted ms-2">Updated: ${new Date(task.updatedAt).toLocaleString()}</small>`;
        }
        html += `</div>
                            <div class="btn-group">
                                <button class="btn btn-sm btn-outline-primary" onclick="editTask(${task.id})">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-success" onclick="updateStatus(${task.id}, 'IN_PROGRESS')">
                                    <i class="fas fa-play"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-success" onclick="updateStatus(${task.id}, 'COMPLETED')">
                                    <i class="fas fa-check"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteTask(${task.id})">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>`;
        return html;
    }).join('');

    taskList.html(map);
}

function saveTask() {
    const taskId = $('#taskId').val();
    const method = taskId ? 'PUT' : 'POST';
    const url = taskId ? `/api/tasks/${taskId}` : '/api/tasks';

    const task = {
        title: $('#title').val(),
        description: $('#description').val(),
        status: $('#status').val()
    };

    $.ajax({
        url: url,
        method: method,
        contentType: 'application/json',
        data: JSON.stringify(task),
        success: function () {
            showAlert(`Task ${taskId ? 'updated' : 'created'} successfully!`, 'success');
            resetForm();
            loadTasks();
        },
        error: function (xhr) {
            showAlert('Error saving task: ' + xhr.responseText, 'danger');
        }
    });
}

function editTask(id) {
    $.ajax({
        url: `/api/tasks/${id}`,
        method: 'GET',
        success: function (task) {
            $('#taskId').val(task.id);
            $('#title').val(task.title);
            $('#description').val(task.description);
            $('#status').val(task.status);
            $('#submitBtn').html('<i class="fas fa-save"></i> Update Task');
            $('#cancelEditBtn').show();
            $('html, body').animate({scrollTop: 0}, 'slow');
        }
    });
}

function updateStatus(id, status) {
    $.ajax({
        url: `/api/tasks/${id}/status?status=${status}`,
        method: 'PUT',
        success: function () {
            showAlert(`Task status updated to ${status}`, 'success');
            loadTasks();
        },
        error: function (xhr) {
            showAlert('Error updating status: ' + xhr.responseText, 'danger');
        }
    });
}

function deleteTask(id) {
    if (!confirm('Are you sure you want to delete this task?')) return;

    $.ajax({
        url: `/api/tasks/${id}`,
        method: 'DELETE',
        success: function () {
            showAlert('Task deleted successfully!', 'success');
            loadTasks();
        },
        error: function (xhr) {
            showAlert('Error deleting task: ' + xhr.responseText, 'danger');
        }
    });
}

function resetForm() {
    $('#taskForm')[0].reset();
    $('#taskId').val('');
    $('#submitBtn').html('<i class="fas fa-plus"></i> Create Task');
    $('#cancelEditBtn').hide();
}

function testKafka() {
    $.ajax({
        url: '/api/tasks',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            title: 'Test Kafka Task',
            description: 'This task was created to test Kafka notifications'
        }),
        success: function () {
            showAlert('Test task created! Check notifications.', 'info');
            loadTasks();
        }
    });
}

function getStatusClass(status) {
    const classes = {
        'PENDING': 'bg-secondary',
        'IN_PROGRESS': 'bg-primary',
        'COMPLETED': 'bg-success',
        'CANCELLED': 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
}

function showAlert(message, type) {
    const alert = $(`
                <div class="alert alert-${type} alert-dismissible fade show position-fixed"
                     style="top: 20px; right: 20px; z-index: 1000;">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            `);

    $('body').append(alert);
    setTimeout(() => alert.alert('close'), 3000);
}
