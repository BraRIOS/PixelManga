'use strict';

(function ($) {

    /*------------------
        Preloader
    --------------------*/
    $(window).on('load', function () {
        $(".loader").fadeOut();
        $("#preloder").delay(200).fadeOut("slow");

        /*------------------
            FIlter
        --------------------*/
        $('.filter__controls li').on('click', function () {
            $('.filter__controls li').removeClass('active');
            $(this).addClass('active');
        });
        if ($('.filter__gallery').length > 0) {
            var containerEl = document.querySelector('.filter__gallery');
            var mixer = mixitup(containerEl);
        }
    });

    /*------------------
        Background Set
    --------------------*/
    $('.set-bg').each(function () {
        var bg = $(this).data('setbg');
        $(this).css('background-image', 'url(' + bg + ')');
    });

    /*------------------
        Lists Thumbnails
    ------------------*/
    $('.thumbnail-list').each(function () {
        let id = parseInt($(this).data('id'))
        let style = document.createElement('style');
        style.textContent= `
                .list-thumbnail-${id}::before{
                    background-image: url('/images/lists/${id}');
                }
            `;
        $(this).addClass(`list-thumbnail-${id}`);
        $(this).removeClass('thumbnail-list');
        $(this).append(style);
    });

    // Search model
    $('.search-switch').on('click', function () {
        $('.search-model').fadeIn(400);
        $('.search-model input').focus();
        $(document).keydown(function (e) {
            if (e.keyCode === 13) {
                $('#search_submit').trigger('click');
            }
            if (e.keyCode === 27) {
                $('.search-close-switch').trigger('click');
            }
        });
    });

    $('.search-close-switch').on('click', function () {
        $('.search-model').fadeOut(400, function () {
            $('#search-input').val('');
        });
    });

    /*------------------
		Navigation
	--------------------*/
    $(".mobile-menu").slicknav({
        prependTo: '#mobile-menu-wrap',
        allowParentLinks: true
    });

    /*------------------
		Hero Slider
	--------------------*/
    let hero_s = $(".hero__slider");
    if(hero_s.length !==0) {
        hero_s.owlCarousel({
            loop: true,
            margin: 0,
            items: 1,
            dots: true,
            nav: true,
            navText: ["<span class='arrow_carrot-left'></span>", "<span class='arrow_carrot-right'></span>"],
            animateOut: 'fadeOut',
            animateIn: 'fadeIn',
            smartSpeed: 1200,
            autoHeight: false,
            autoplay: true,
            mouseDrag: false
        });
    }

    /*------------------
        Scroll To Top
    --------------------*/
    $("#scrollToTopButton").click(function() {
        $("html, body").animate({ scrollTop: 0 }, "slow");
        return false;
    });

    /*------------------
        Profile Menu
    --------------------*/

    $('.open-userMenu').on('click', function (e) {
        e.preventDefault();
        $(".erc-user-menu").toggleClass("state-open");
        $(".erc-page-overlay").toggleClass("state-open");
        $("body").toggleClass("state-scroll-blocked");
    });
    $('.erc-page-overlay').on('click', function (e) {
        e.preventDefault();
        $(".erc-user-menu").toggleClass("state-open");
        $(".erc-page-overlay").toggleClass("state-open");
        $("body").toggleClass("state-scroll-blocked");
    });
    $('#logout').click(function () {
        $("#logout-form").submit();
    });

    /*------------------
        Premium menu
    --------------------*/
    $.get('isPremium', function(data) {
        if (data) {
            $("#premiumBadge").removeClass("d-none");
            $("#showSubscription").removeClass("d-none");
            $("#buySubscription").addClass("d-none");
        } else {
            $("#premiumBadge").addClass("d-none");
            $("#showSubscription").addClass("d-none");
            $("#buySubscription").removeClass("d-none");
        }
    });

})(jQuery);