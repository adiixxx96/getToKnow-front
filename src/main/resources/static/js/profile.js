
function showSection(sectionId, element) {
	$('.content-section').hide();
	$('#' + sectionId).show();

	$('.tabs .btn').removeClass('active');
	$(element).addClass('active');
}

//Function to hide/show password
function togglePassword() {
	var passwordInput = document.getElementById("password");
	var toggleIcon = document.querySelector(".toggle-password");

	if (passwordInput.type === "password") {
		passwordInput.type = "text";
		toggleIcon.classList.remove("fa-eye");
		toggleIcon.classList.add("fa-eye-slash");
	} else {
		passwordInput.type = "password";
		toggleIcon.classList.remove("fa-eye-slash");
		toggleIcon.classList.add("fa-eye");
	}
}

function validateData(form) {
	event.preventDefault();

	let fullname = $("#fullname").val().trim();
	let password = $("#password").val().trim();

	let error = false;

	if (fullname === '') {
		$("#fullnameError").show();
		error = true;
	} else {
		$("#fullnameError").hide();
	}

	if (password === '' || password.length < 8) {
		$("#passwordError").show();
		error = true;
	} else {
		$("#passwordError").hide();
	}

	if (error) return;

	form.submit();
}

function disjoinEvent(eventId) {

	//Disable disjoin event button
	const button = $(`.disjoinEventBtn[data-event-id='${eventId}']`).first();
	button.prop('disabled', true);
	button.addClass('disabled');
	button.removeAttr('onclick');

	$.ajax({
		url: `/event/disjoin/${eventId}`,
		type: 'POST',
		contentType: 'application/json',
		success: function(data) {
			window.scrollTo({ top: 0, behavior: 'smooth' });
			const successMessage = 'Te has desapuntado del evento';
			showJsMessage(successMessage, 'success');
			setTimeout(() => {
				window.location.href = `/user/profile/`;
			}, 2000);
		},
		error: function(xhr) {
			window.scrollTo({ top: 0, behavior: 'smooth' });
			const errorMessage = 'Ha ocurrido un error al desapuntarte del evento';
			showJsMessage(errorMessage, 'error');
			setTimeout(() => {
				window.location.href = `/user/profile/`;
			}, 2000);
		}
	});
}

//Open select file when clicking on input fake shown
	function triggerFileInput() {
	    $('#profileImage').click();
	}

	$('#profileImage').on('change', function () {
	    const fileName = this.files[0]?.name || 'Haz clic para seleccionar un archivo';
	    $('#fakeInput').val(fileName);
	});