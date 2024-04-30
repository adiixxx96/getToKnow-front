$('input[name="birthDate"].dateInput').daterangepicker({
    autoUpdateInput: false,
    singleDatePicker: true,
    opens: 'center',
    drops: 'auto',
    locale: {
        format: 'DD/MM/YYYY',
        applyLabel: 'Aplicar',
        cancelLabel: 'Cancelar',
        daysOfWeek: ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'],
        monthNames: [
            'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
            'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ]
    }
});

	$('input[name="birthDate"].dateInput')
		.on('apply.daterangepicker', function(ev, picker) {
			$(this).val(picker.startDate.format('DD/MM/YYYY')).trigger("change");
		})
		.on('cancel.daterangepicker', function(ev, picker) {
			$(this).val('').trigger("change");
		});

//Function to hide/show password in forms with passwords
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

document.querySelector(".toggle-password").addEventListener("click", function() {
    togglePassword();
});

function validateData(form) {
	event.preventDefault();
	
	var fullname = $("#fullname").val().trim();
	var email = $("#email").val().trim();
	var password = $("#password").val().trim();
	var birthDateString = $("#birthDate").val().trim();
	var acceptTerms = $("#acceptTerms").is(":checked");
	
	var error = false;
	
	if (fullname === '') {
        $("#fullnameError").show();
        error = true;
    } else {
        $("#fullnameError").hide();
    }
    
    if (email === '' || !email.includes('@')) {
        $("#emailError").show();
        error = true;
    } else {
        $("#emailError").hide();
    }
    
    if (password === '' || password.length < 8) {
        $("#passwordError").show();
        error = true;
    } else {
         $("#passwordError").hide();
    }
    
    if (birthDateString === '') {
        $("#birthDateError").show();
        error = true;
    } else {
         $("#birthDateError").hide();
    }
    
    if (!acceptTerms) {
		$("#acceptTermsError").show();
        error = true;
    } else {
         $("#acceptTermsError").hide();
    }
    
    var birthDateRegex = /^\d{2}\/\d{2}\/\d{4}$/;
      if (!birthDateRegex.test(birthDateString)) {
		 $("#birthDateError").show();
		 error = true; 
	  } else {
		 $("#birthDateError").hide();
	  }
	 
	if (error) return;
	
	form.submit();
		
}