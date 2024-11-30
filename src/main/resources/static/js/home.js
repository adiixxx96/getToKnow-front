//Function to be able to show tooltips
$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
    
 });

//Update tooltip if button join disabled
function updateTooltipContent(buttonElement, content) {
    buttonElement.title = content;
}

//Disable button if user not loggued or max participants number reached
var buttons = document.querySelectorAll('.joinEventBtn');

buttons.forEach(function(button) {
	let tooltip = new bootstrap.Tooltip(button);
        let eventId = parseInt(button.dataset.eventId);

        // Find the corresponding event
        let e = events.find(function(event) {
            return event.id === eventId;
        });

        let isLoggedIn = true;
        let isMaxParticipantsReached = false;
        let isUserAlreadyJoined = false;

        if (e) {
            if (!isLogged) { 
                isLoggedIn = false;
           } else if (e.users.some(function(user) {
			    return user.user.id === userId && (!user.deregistrationDate || user.deregistrationDate === null);
			})) {
			    isUserAlreadyJoined = true;
            } else if (e.users.filter(function(user) {
			    return !user.deregistrationDate || user.deregistrationDate === null;
			}).length - 1 >= e.maxParticipants) {
			    isMaxParticipantsReached = true;
			}

            if (!isLogged || isMaxParticipantsReached || isUserAlreadyJoined) {
                if (!isLoggedIn) {
					tooltipContent = "Debes iniciar sesión con tu usuario para poder apuntarte a un evento.";
                } else if (isUserAlreadyJoined) {
					tooltipContent = "Ya estás apuntado a este evento.";
                } else {
                    tooltipContent = "Se ha alcanzado el número máximo de personas apuntadas.";
                }
                
                updateTooltipContent(button, tooltipContent);
                button.disabled = true;
                button.classList.add('disabled');
                button.removeAttribute('onclick');
            }
        }  
});

function joinEvent(eventId) {
    $.ajax({
        url: `/event/join/${eventId}`,
        type: 'POST',
        contentType: 'application/json',
        success: function(data) {
			window.scrollTo({top: 400, behavior: 'smooth'});
			const successMessage = 'Te has registrado en el evento';
            showJsMessage(successMessage, 'success');
            setTimeout(() => { 
            	window.location.href = `/event/detail/${eventId}`;
            }, 2000);
        },
        error: function(xhr) {
			window.scrollTo({top: 0, behavior: 'smooth'});
            const errorMessage = 'Ha ocurrido un error al apuntarte al evento';
            showJsMessage(errorMessage, 'error');
        }
    });
}