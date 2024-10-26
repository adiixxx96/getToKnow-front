function showSection(sectionId, element) {
    $('.content-section').hide();
    $('#' + sectionId).show();
    
    $('.tabs .btn').removeClass('active');
    $(element).addClass('active');
}

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