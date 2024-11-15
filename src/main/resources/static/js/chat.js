function disableLink(element) {
    element.classList.add("disabled-link");
}

updateLastMessageMargin();

$('#confirmBlockModal').on('show.bs.modal', function(event) {
    const button = $(event.relatedTarget);
    const chatId = button.data('chat-id');
    const userId = button.data('user-to-id');

    $(this).data('chat-id', chatId);
    $(this).data('user-id', userId);
});


$('#modalConfirmBlock').on('click', function() {
    const chatId = $('#confirmBlockModal').data('chat-id');
    const userId = $('#confirmBlockModal').data('user-id');
    const reason = $('#literal').val();

    if (!reason) {
        alert("Por favor, selecciona un motivo para el bloqueo.");
        return;
    }

    $.ajax({
        url: '/chat/blockUser',
        type: 'POST',
        data: {
            chatId: chatId,
            userId: userId,
            reason: reason
        },
        success: function(response) {
            $('#confirmBlockModal').modal('hide');
            window.scrollTo({top: 0, behavior: 'smooth'});
			const successMessage = 'El usuario ha sido bloqueado.';
            showJsMessage(successMessage, 'success');
            setTimeout(() => { 
            	window.location.href = `/chat/list/`;
            }, 2000);
        },
        error: function() {
            window.scrollTo({top: 0, behavior: 'smooth'});
            const errorMessage = 'Ha habido un error al bloquear al usuario';
            showJsMessage(errorMessage, 'error');
            setTimeout(() => { 
            	window.location.href = `/chat/list/`;
            }, 2000);
        }
    });
});

let stompClient = null;

// Connect to WebSocket when the page loads
function connectWebSocket() {
    const socket = new SockJS('/chat-websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        // Subscribe to messages for the specific chat
        stompClient.subscribe(`/topic/chat/${chatId}`, function (message) {
			const messageData = JSON.parse(message.body);
            
            // Check if it is a new message or an read update
            if (messageData.type === "updateReadStatus") {
                updateAllReadStatusInUI();
            } else {
                // Si no es una actualización, es un mensaje nuevo
                if (!messageData.read && messageData.userId !== userId) {
                    messageData.read = true;
                    stompClient.send("/app/chat.messageRead", {}, JSON.stringify(messageData));
                }
                displayMessage(messageData);
            }
            
        });
    });
}

// Send the message to the server
function sendMessage(chatId, userId) {
	//Validate message
	const messageText = $('#message').val();
    if (!messageText.trim()) {
        alert("El mensaje no puede estar vacío.");
        return;
    }
    if (messageText.length > 3000) {
        alert("El mensaje es demasiado largo");
        return;
    }
	
    const message = {
        chatId: chatId,
        message: messageText,
        userId: userId,
        creationDate: new Date(),
        read: false
    };

    stompClient.send(`/app/chat.sendMessage`, {}, JSON.stringify(message));
    $('#message').val('');
}

// Display the message in the container
function displayMessage(message) {
    const messageContainer = $('#message-container');
    
    // Check if the message already exists using its ID
    let existingMessage = $(`#message-${message.id}`);
    
    // If the message doesn't exist, create a new one
    if (existingMessage.length === 0) {
        const messageBubbleContainer = $('<div>');
        messageBubbleContainer.addClass("message-bubble-container");
        messageBubbleContainer.addClass(message.userId === userId ? "own-message" : "other-message");

        const messageBubble = $('<div class="message-bubble">');
        messageBubble.append(`<p>${message.message}</p>`);

        const messageInfo = $('<div class="message-info">');
        messageInfo.append(`<span class="creation-date">${new Date(message.creationDate).toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })} 
        ${new Date(message.creationDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>`);

        if (message.userId === userId) {
            const readIcon = $('<i>');
            readIcon.addClass(message.read ? 'fa-solid fa-check-double read' : 'fa-solid fa-check unread');
            messageInfo.append(readIcon);
        }

        messageBubble.append(messageInfo);
        messageBubbleContainer.append(messageBubble);
        messageBubbleContainer.attr('id', `message-${message.id}`);
        messageContainer.append(messageBubbleContainer);
        
    } else {
        // If the message already exists (we're only updating the read status)
        if (message.userId === userId) {
            const readIcon = existingMessage.find('.message-info i');
            if (readIcon && message.read) {
                readIcon.removeClass('fa-solid fa-check unread').addClass('fa-solid fa-check-double read');
            }
        }
    }

    updateLastMessageMargin();
    scrollToBottom();
}

function updateAllReadStatusInUI() {
    $('.own-message .unread').each(function () {
        $(this).removeClass('fa-check unread').addClass('fa-check-double read');
    });
}

if (typeof chatId !== 'undefined' && chatId) {
    connectWebSocket();
}

$('#sendButton').click(function() {
   sendMessage(chatId, userId);
});

const messageContainer = $('#message-container');
    messageContainer.scrollTop(messageContainer.prop('scrollHeight'));
    
function scrollToBottom() {
    const messageContainer = $('#message-container');
    messageContainer.scrollTop(messageContainer.prop('scrollHeight'));
}

function updateLastMessageMargin() {
    const messages = $('.message-bubble');
    messages.removeClass('last-message');
    
    if (messages.length > 0) {
        $(messages[messages.length - 1]).addClass('last-message');
    }
}

// Check if send message should be disabled'
if (typeof chatId !== 'undefined' && chatId) {
	const cardActionsExist = $('.cardActions').length > 0;
	if (chatStatus === false || cardActionsExist) {
		$('#message').prop('disabled', true);
		$('#sendButton').prop('disabled', true);
		
		if (chatStatus === false) {
			$('#message').attr('title', 'Ya no puedes mandar mensajes porque el chat está inactivo');
			$('#sendButton').attr('title', 'Ya no puedes mandar mensajes porque el chat está inactivo');	
		} else if (cardActionsExist) {
			$('#message').attr('title', 'No puedes mandar mensajes hasta que no aceptes la solicitud');
			$('#sendButton').attr('title', 'No puedes mandar mensajes hasta que no aceptes la solicitud');	
		}
		
		$('#message, #sendButton').tooltip({
            trigger: 'hover',
            placement: 'top'
        });	
	}
}
