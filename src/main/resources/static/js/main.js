//Function to show tooltips
$(document).ready(function() {
	$('[data-toggle="tooltip"]').tooltip();

	//Notifications
	$('.notification-button').on('click', function() {
		$('#notification-popup').toggleClass('d-none');
	});
});

//Function to show section in admin / profile page
function showSection(sectionId, element) {
    $('.content-section').hide();
    $('#' + sectionId).show();
    
    $('.tabs .btn').removeClass('active');
    $(element).addClass('active');
}

$(document).on('click', '#notification-popup .list-group-item a', function(e) {
	e.preventDefault();
	const notificationId = $(this).closest('.list-group-item').data('id');

	markAsRead(notificationId);

	setTimeout(function() {
		const href = $(this).attr('href');
		if (href) {
			window.location.href = href;
		}
	}.bind(this), 1000);

});

function showJsMessage(message, type) {
	const jsMessagesContainer = document.getElementById('messagesJs');
	const jsSuccessMessage = document.getElementById('jsSuccessMessage');
	const jsErrorMessage = document.getElementById('jsErrorMessage');

	if (type === 'success') {
		jsSuccessMessage.innerText = message;
		jsMessagesContainer.querySelector('.alert-success').classList.add('show-important');
		jsMessagesContainer.querySelector('.alert-success').classList.remove('hide-important');
		jsMessagesContainer.querySelector('.alert-danger').classList.add('hide-important');
		jsMessagesContainer.querySelector('.alert-danger').classList.remove('show-important');
	} else if (type === 'error') {
		jsErrorMessage.innerText = message;
		jsMessagesContainer.querySelector('.alert-danger').classList.add('show-important');
		jsMessagesContainer.querySelector('.alert-danger').classList.remove('hide-important');
		jsMessagesContainer.querySelector('.alert-success').classList.add('hide-important');
		jsMessagesContainer.querySelector('.alert-success').classList.remove('show-important');
	}

	jsMessagesContainer.style.display = 'block';
}

function markAsRead(notificationId) {
	$.ajax({
		url: `/notification/markAsRead/${notificationId}`,
		type: 'POST',
		contentType: 'application/json',
		success: function(data) {
			$(`#notification-popup .list-group-item[data-id="${notificationId}"]`).remove();
			const badge = $('.notification-button .badge');
			let currentCount = parseInt(badge.text(), 10);
			if (!isNaN(currentCount) && currentCount > 0) {
				badge.text(currentCount - 1);
				if (currentCount - 1 === 0) {
					badge.addClass('d-none');
				}
			}
			if ($('#notification-popup .list-group-item').length === 0) {
				$('#notification-popup .notificationsLive').removeClass('d-none');
				$('#notification-popup .list-group').addClass('d-none');
			}
		},
		error: function(xhr) {
		}
	});
};