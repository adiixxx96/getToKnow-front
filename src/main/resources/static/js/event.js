$(document).ready(function() {
	$('[data-toggle="tooltip"]').tooltip();
	
	updateJoinEventButtons()
	
	//Price range slider
	var priceSlider = document.getElementById('price-slider');
	var priceFrom = $('#priceFrom');
	var priceTo = $('#priceTo');

	noUiSlider.create(priceSlider, {
		start: [0, 500],
		connect: true,
		range: {
			'min': 0,
			'max': 500
		}
	});

	priceSlider.noUiSlider.on('update', function(values, handle) {
		var value = Math.round(values[handle]);

		if (handle) {
			priceTo.val(value === 500 ? '+500' : value);
		} else {
			priceFrom.val(value);
		}
	});

	function updateSliderPrice() {
		var fromVal = priceFrom.val() === '+500' ? 500 : priceFrom.val();
		var toVal = priceTo.val() === '+500' ? 500 : priceTo.val();
		priceSlider.noUiSlider.set([fromVal, toVal]);
	}

	priceFrom.on('change', updateSliderPrice);
	priceTo.on('change', updateSliderPrice);
	
	//Participants range slider
	var participantsSlider = document.getElementById('participants-slider');
	var participantsFrom = $('#participantsFrom');
	var participantsTo = $('#participantsTo');

	noUiSlider.create(participantsSlider, {
		start: [0, 100],
		connect: true,
		range: {
			'min': 0,
			'max': 100
		}
	});

	participantsSlider.noUiSlider.on('update', function(values, handle) {
		var value = Math.round(values[handle]);

		if (handle) {
			participantsTo.val(value === 100 ? '+100' : value);
		} else {
			participantsFrom.val(value);
		}
	});

	function updateSliderParticipants() {
		var fromVal = participantsFrom.val() === '+100' ? 100 : participantsFrom.val();
		var toVal = participantsTo.val() === '+100' ? 100 : participantsTo.val();
		participantsSlider.noUiSlider.set([fromVal, toVal]);
	}

	participantsFrom.on('change', updateSliderParticipants);
	participantsTo.on('change', updateSliderParticipants);
	
	//Pagination
	updatePagination();
  
});

//Calendar input
var now = moment();
var today = moment().startOf('day');
$('input[name="eventDate"].dateInput').daterangepicker({
	autoUpdateInput: false,
	singleDatePicker: true,
	timePicker: true,
	timePicker24Hour: true,
	opens: 'center',
	drops: 'auto',
	minDate: now,
	locale: {
		format: 'DD/MM/YYYY   HH:mm',
		applyLabel: 'Aplicar',
		cancelLabel: 'Cancelar',
		daysOfWeek: ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'],
		monthNames: [
			'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
			'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
		]
	}
});

$('input[name="eventDate"].dateInput')
	.on('apply.daterangepicker', function(ev, picker) {
		$(this).val(picker.startDate.format('DD/MM/YYYY   HH:mm')).trigger("change");
	})
	.on('cancel.daterangepicker', function(ev, picker) {
		$(this).val('').trigger("change");
	});

//Calendar input for date filters
$('#eventDateFrom').daterangepicker({
	singleDatePicker: true,
	autoUpdateInput: false,
	minDate: today,
	opens: 'center',
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
}).on('apply.daterangepicker', function(ev, picker) {
	$(this).val(picker.startDate.format('DD/MM/YYYY')).trigger("change");
}).on('cancel.daterangepicker', function(ev, picker) {
	$(this).val('').trigger("change");
});

$('#eventDateTo').daterangepicker({
	singleDatePicker: true,
	autoUpdateInput: false,
	minDate: today,
	opens: 'center',
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
}).on('apply.daterangepicker', function(ev, picker) {
	$(this).val(picker.startDate.format('DD/MM/YYYY')).trigger("change");
}).on('cancel.daterangepicker', function(ev, picker) {
	$(this).val('').trigger("change");
});

//Max participants input
$('.btn-number').click(function(e) {
	e.preventDefault();

	fieldName = $(this).attr('data-field');
	type = $(this).attr('data-type');
	var input = $("input[name='" + fieldName + "']");
	var currentVal = parseInt(input.val());

	if (!isNaN(currentVal)) {
		if (type == 'minus') {
			if (currentVal > input.attr('min')) {
				input.val(currentVal - 1).change();
			}
			if (parseInt(input.val()) == input.attr('min')) {
				$(this).attr('disabled', true);
			}
		} else if (type == 'plus') {
			input.val(currentVal + 1).change();

		}
	} else {
		input.val(input.attb('min'));
	}
});
$('.input-number').focusin(function() {
	$(this).data('oldValue', $(this).val());
});
$('.input-number').change(function() {

	minValue = parseInt($(this).attr('min'));
	valueCurrent = parseInt($(this).val());

	inputName = $(this).attr('name');
	if (valueCurrent >= minValue) {
		$(".btn-number[data-type='minus'][data-field='" + inputName + "']").removeAttr('disabled')
	} else {
		$(this).val($(this).data('oldValue'));
	}
});
$(".input-number").keydown(function(e) {
	if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 190]) !== -1 ||
		(e.keyCode == 65 && (e.ctrlKey === true || e.metaKey === true)) ||
		(e.keyCode >= 35 && e.keyCode <= 40)) {
		return;
	}
	if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
		e.preventDefault();
	}
});

//Textarea check maximum characters reached
$(document).ready(function() {
    var maxLength = $('#description').attr('maxlength');
    var currentLength = $('#description').val().length;

    $('#charCount').text(currentLength + ' / ' + maxLength + ' caracteres');

    $('#description').on('input', function() {
        var maxLength = $(this).attr('maxlength');
        var currentLength = $(this).val().length;

        $('#charCount').text(currentLength + ' / ' + maxLength + ' caracteres');

        if (currentLength > maxLength) {
            $(this).val($(this).val().substring(0, maxLength));
        }
    });
});

	$('#comment').on('input', function() {
		var maxLength = $(this).attr('maxlength');
		var currentLength = $(this).val().length;

		$('#charCountComment').text(currentLength + ' / ' + maxLength + ' caracteres');

		if (currentLength > maxLength) {
			$(this).val($(this).val().substring(0, maxLength));
		}
	});
	
	$('.reply').on('input', function() {
	    var maxLength = $(this).attr('maxlength');
	    var currentLength = $(this).val().length;
	
	    $(this).closest('.reply-form').find('.charCountReply').text(currentLength + ' / ' + maxLength + ' caracteres');
	
	    if (currentLength > maxLength) {
	        $(this).val($(this).val().substring(0, maxLength));
	    }
	});

//Categories selection
var categories = document.querySelectorAll('.categoryCard');
var selectedCategoryIdInput = document.getElementById('category');

categories.forEach(function(category) {
	category.addEventListener('click', function() {
		categories.forEach(function(c) {
			c.classList.remove('selected');
		});

		this.classList.add('selected');
		var categoryId = this.dataset.id;
		selectedCategoryIdInput.value = categoryId;
	});
});

//Function to select categorycards and change color
function selectCard(card) {
	var cards = document.getElementsByClassName('categoryCard');
	for (var i = 0; i < cards.length; i++) {
		cards[i].classList.remove('selected');
	}

	card.classList.add('selected');
}

//Funcion to select tag pills and change color
function selectTag(tagPill) {
	var tagId = tagPill.dataset.id;
	var selectedTagsInput = document.getElementById('tags');
	var selectedTags = selectedTagsInput.value.split(',').filter(Boolean);

	var index = selectedTags.indexOf(tagId.toString());
	if (index === -1) {
		if (selectedTags.length >= 5) {
			return; // Si ya hay 5, no agregar más
		}
		selectedTags.push(tagId);
	} else {
		selectedTags.splice(index, 1);
	}

	selectedTagsInput.value = selectedTags.join(',');

	tagPill.classList.toggle('selected');
}

function validateData(form) {
	event.preventDefault();

	let title = $("#title").val().trim();
	let eventDate = $("#eventDate").val().trim();
	var description = $("#description").val().trim();
	let address = $("#address").val().trim();
	let province = $("#province").val().trim();
	let city = $("#city").val().trim();
	let price = $("#price").val().trim();
	let category = $("#province").val().trim();
	let maxParticipants = $("#maxParticipants").val().trim();

	let error = false;

	if (title === '' || title.length > 50) {
		$("#titleError").show();
		error = true;
	} else {
		$("#titleError").hide();
	}

	if (eventDate === '') {
		$("#eventDateError").show();
		error = true;
	} else {
		$("#eventDateError").hide();
	}

	let eventDateRegex = /^\d{2}\/\d{2}\/\d{4}\s+\d{2}:\d{2}$/;
	if (!eventDateRegex.test(eventDate)) {
		$("#eventDateError").show();
		error = true;
	} else {
		$("#eventDateError").hide();
	}

	if (description === '') {
		$("#descriptionError").show();
		error = true;
	} else {
		$("#descriptionError").hide();
	}

	if (address === '') {
		$("#addressError").show();
		error = true;
	} else {
		$("#addressError").hide();
	}

	if (province === '') {
		$("#provinceError").show();
		error = true;
	} else {
		$("#provinceError").hide();
	}

	if (city === '') {
		$("#cityError").show();
		error = true;
	} else {
		$("#cityError").hide();
	}

	let priceRegex = /^\d+(\.\d{1,2})?$/;
	if (!priceRegex.test(price)) {
		$("#priceError").show();
		error = true;
	} else {
		$("#priceError").hide();
	}

	if (category === undefined || category === '') {
		$("#categoryError").show();
		error = true;
	} else {
		$("#categoryError").hide();
	}
	
	if (existingEvent) {
		if (maxParticipants < existingEvent.users.length -1) {
			$("#maxParticipantsError").show();
			error = true;
		} else {
			$("#maxParticipantsError").hide();
		}
	}

	if (error) {
		window.scrollTo({top: 0, behavior: 'smooth'});
            const errorMessage = 'Hay algún dato erróneo, revisa el evento.';
            showJsMessage(errorMessage, 'error');
	} else {
		form.submit();
	}

}

function validateComment(form, type, commentId) {
    let comment = '';
    let error = false;

    if (type === 'comment') {
        comment = $("#comment").val().trim();
        if (comment === '' || comment.length > 255) {
            $("#commentError").show();
            error = true;
        } else {
            $("#commentError").hide();
        }
    } else if (type === 'reply') {
        comment = $("#reply-" + commentId).val().trim();
        if (comment === '' || comment.length > 255) {
            $("#replyError-" + commentId).show();
            error = true;
        } else {
            $("#replyError-" + commentId).hide();
        }
    }

    if (error) return;

    form.submit();
}

function resetFilters() {
    $('input[type="text"]').val('');
    $('#province').val('').trigger('change');
    $('input[type="checkbox"]').prop('checked', false);
    
    var priceSlider = document.getElementById('price-slider');
    if (priceSlider) {
        priceSlider.noUiSlider.reset();
    }

    var participantsSlider = document.getElementById('participants-slider');
    if (participantsSlider) {
        participantsSlider.noUiSlider.reset();
    }
    
    search(true);
}

function showReplyForm(commentId) {
   $('#reply-form-' + commentId).slideDown();
}

function search(isFiltering = false) {
	let searchData = {};
	
	if (isFiltering) {
        $('.page-item').removeClass('active');
        $('.page-item').eq(1).addClass('active');
    }
	
	let activePage = $('.page-item.active .page-link').data('page');
	searchData['page'] = activePage;
	
	//Body, city, event dates, price, participants
    $('input[type="text"]').each(function() {
        searchData[$(this).attr('id')] = $(this).val();
    });

    //Province
    searchData['province'] = $('#province').val();

    //Categories
    searchData['categories'] = [];
    $('input[name="categories"]:checked').each(function() {
        searchData['categories'].push($(this).val());
    });
    
    //Tags
    searchData['tags'] = [];
    $('input[name="tags"]:checked').each(function() {
        searchData['tags'].push($(this).val());
    });
    
    $.ajax({
        url: '/event/search',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(searchData),
        success: function(response) {
			$('#eventContainer').empty();
        	$('#eventContainer').html(response);
        	updateJoinEventButtons()
        },
        error: function(error) {
           console.log(error);
        }
    });
}

function joinEvent(eventId) {
    
    //Disable join event button
    const buttons = document.querySelectorAll(`button[data-event-id='${eventId}']`);
	buttons.forEach(button => {
	    button.disabled = true;
	    button.classList.add('disabled');
	    button.removeAttribute('onclick');
	});
    
    $.ajax({
        url: `/event/join/${eventId}`,
        type: 'POST',
        contentType: 'application/json',
        success: function(data) {
			window.scrollTo({top: 0, behavior: 'smooth'});
			updateJoinEventButtons()
			const successMessage = 'Te has registrado en el evento';
            showJsMessage(successMessage, 'success');
            setTimeout(() => { 
            	window.location.href = `/event/detail/${eventId}`;
            }, 2000);
        },
        error: function(xhr) {
			updateJoinEventButtons()
			window.scrollTo({top: 0, behavior: 'smooth'});
            const errorMessage = 'Ha ocurrido un error al apuntarte al evento';
            showJsMessage(errorMessage, 'error');
        }
    });
}

function disjoinEvent(eventId) {
    
    //Disable disjoin event button
    const buttons = document.querySelectorAll(`button[data-event-id='${eventId}']`);
	buttons.forEach(button => {
	    button.disabled = true;
	    button.classList.add('disabled');
	    button.removeAttribute('onclick');
	});
    
    $.ajax({
        url: `/event/disjoin/${eventId}`,
        type: 'POST',
        contentType: 'application/json',
        success: function(data) {
			window.scrollTo({top: 0, behavior: 'smooth'});
			const successMessage = 'Te has desapuntado del evento';
            showJsMessage(successMessage, 'success');
            setTimeout(() => { 
            	window.location.href = `/event/detail/${eventId}`;
            }, 2000);
        },
        error: function(xhr) {
			window.scrollTo({top: 0, behavior: 'smooth'});
            const errorMessage = 'Ha ocurrido un error al desapuntarte del evento';
            showJsMessage(errorMessage, 'error');
            setTimeout(() => { 
            	window.location.href = `/event/detail/${eventId}`;
            }, 2000);
        }
    });
}

function updateTooltipContent(buttonElement, content) {
    buttonElement.title = content;
}

// Disable button if user not logged or max participants number reached
function updateJoinEventButtons() {
    var buttons = document.querySelectorAll('.joinEventBtn');

    buttons.forEach(function(button) {
        // Initialize bootstrap tooltip for each button
        let tooltip = new bootstrap.Tooltip(button);
        let eventId = parseInt(button.dataset.eventId);

        // Find the corresponding event
        let e = (typeof events !== 'undefined' && Array.isArray(events)) 
    		? events.find(event => event.id === eventId) 
    		: (typeof event !== 'undefined' ? event : null);

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
}

$('#confirmReportModal').on('show.bs.modal', function(event) {
    const button = $(event.relatedTarget);
    const eventId = button.data('event-id');
    const userId = button.data('user-id');

    $(this).data('event-id', eventId);
    $(this).data('user-id', userId);
});

$('#modalConfirmReport').on('click', function() {
    const eventId = $('#confirmReportModal').data('event-id');
    const userId = $('#confirmReportModal').data('user-id');
    const reason = $('#literal').val();

    if (!reason) {
        alert("Por favor, selecciona un motivo para el bloqueo.");
        return;
    }

    $.ajax({
        url: '/event/reportEvent',
        type: 'POST',
        data: {
            eventId: eventId,
            userId: userId,
            reason: reason
        },
        success: function(response) {
            $('#confirmReportModal').modal('hide');
            window.scrollTo({top: 0, behavior: 'smooth'});
			const successMessage = 'El evento ha sido reportado.';
            showJsMessage(successMessage, 'success');
            setTimeout(() => { 
            	window.location.href = `/event/detail/${eventId}`;
            }, 2000);
        },
        error: function() {
            window.scrollTo({top: 0, behavior: 'smooth'});
            const errorMessage = 'Ha habido un error reportando el evento';
            showJsMessage(errorMessage, 'error');
            setTimeout(() => { 
            	window.location.href = `/event/detail/${eventId}`;
            }, 2000);
        }
    });
});

//Pagination functions
function changePage(page) {
        $('.page-item').removeClass('active');
        $('[data-page="' + page + '"]').parent().addClass('active');
        window.scrollTo({top: 130, behavior: 'smooth'});
        search();
        updatePagination();
    }

    function updatePagination() {
        const currentPage = parseInt($('.page-item.active .page-link').data('page'));
        $('#prevBtn').prop('disabled', currentPage === 1);
        $('#nextBtn').prop('disabled', currentPage === totalPages);
    }

    function prevPage() {
        const currentPage = parseInt($('.page-item.active .page-link').data('page'));
        if (currentPage > 1) {
            changePage(currentPage - 1);
        }
    }

    function nextPage() {
        const currentPage = parseInt($('.page-item.active .page-link').data('page'));
        if (currentPage < totalPages) {
            changePage(currentPage + 1);
        }
    }