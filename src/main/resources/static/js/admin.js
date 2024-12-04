function showSection(sectionId, element) {
    $('.content-section').hide();
    $('#' + sectionId).show();
    
    $('.tabs .btn').removeClass('active');
    $(element).addClass('active');
}