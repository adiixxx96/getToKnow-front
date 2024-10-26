//Function to be able to show tooltips
$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
    
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