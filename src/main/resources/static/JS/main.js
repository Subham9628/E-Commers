// ============================================
// ShopSphere E-Commerce System - Main JavaScript
// ============================================

// Wait for DOM to load
$(document).ready(function() {
    console.log("ShopSphere JavaScript loaded");
    
    // Initialize all components
    initQuantityButtons();
    initProductFilters();
    initAddToCart();
    initWishlist();
    initImageGallery();
    initFormValidation();
    initTooltips();
});

// ============================================
// Cart Functions
// ============================================

function initQuantityButtons() {
    $('.quantity-btn').click(function() {
        let input = $(this).siblings('.quantity-input');
        let currentVal = parseInt(input.val());
        
        if ($(this).hasClass('plus')) {
            input.val(currentVal + 1);
        } else if ($(this).hasClass('minus') && currentVal > 1) {
            input.val(currentVal - 1);
        }
        
        updateCartItem($(this).data('item-id'), input.val());
    });
}

function updateCartItem(itemId, quantity) {
    $.ajax({
        url: '/cart/update/' + itemId,
        method: 'POST',
        data: { quantity: quantity },
        success: function(response) {
            updateCartTotal();
            showNotification('Cart updated successfully!', 'success');
        },
        error: function() {
            showNotification('Error updating cart', 'error');
        }
    });
}

function updateCartTotal() {
    let total = 0;
    $('.cart-item-subtotal').each(function() {
        total += parseFloat($(this).text());
    });
    $('#cart-total').text('$' + total.toFixed(2));
}

// ============================================
// Add to Cart Function
// ============================================

function initAddToCart() {
    $('.add-to-cart-btn').click(function(e) {
        e.preventDefault();
        let productId = $(this).data('product-id');
        let quantity = $('.quantity-input').val() || 1;
        
        showLoading();
        
        $.ajax({
            url: '/cart/add',
            method: 'POST',
            data: {
                productId: productId,
                quantity: quantity
            },
            success: function(response) {
                hideLoading();
                showNotification('Product added to cart!', 'success');
                updateCartCount();
            },
            error: function(xhr) {
                hideLoading();
                if (xhr.status === 401) {
                    showNotification('Please login to add items to cart', 'warning');
                    setTimeout(function() {
                        window.location.href = '/login';
                    }, 2000);
                } else {
                    showNotification('Error adding product to cart', 'error');
                }
            }
        });
    });
}

function updateCartCount() {
    $.ajax({
        url: '/cart/count',
        method: 'GET',
        success: function(count) {
            $('.cart-count').text(count);
            $('.cart-badge').text(count);
        }
    });
}

// ============================================
// Wishlist Functions
// ============================================

function initWishlist() {
    $('.wishlist-btn').click(function(e) {
        e.preventDefault();
        let productId = $(this).data('product-id');
        let $btn = $(this);
        
        $.ajax({
            url: '/wishlist/toggle',
            method: 'POST',
            data: { productId: productId },
            success: function(response) {
                if (response.added) {
                    $btn.addClass('liked');
                    showNotification('Added to wishlist!', 'success');
                } else {
                    $btn.removeClass('liked');
                    showNotification('Removed from wishlist', 'info');
                }
            }
        });
    });
}

// ============================================
// Product Filters
// ============================================

function initProductFilters() {
    $('#price-range').on('input', function() {
        $('#price-value').text('$' + $(this).val());
    });
    
    $('.filter-checkbox').change(function() {
        applyFilters();
    });
    
    $('#sort-by').change(function() {
        applyFilters();
    });
}

function applyFilters() {
    let filters = {
        category: $('#category-filter').val(),
        minPrice: $('#min-price').val(),
        maxPrice: $('#max-price').val(),
        sort: $('#sort-by').val(),
        sizes: getSelectedValues('size-filter'),
        colors: getSelectedValues('color-filter')
    };
    
    let queryString = $.param(filters);
    window.location.href = '/products?' + queryString;
}

function getSelectedValues(className) {
    let values = [];
    $('.' + className + ':checked').each(function() {
        values.push($(this).val());
    });
    return values;
}

// ============================================
// Image Gallery
// ============================================

function initImageGallery() {
    $('.thumbnail').click(function() {
        let mainImage = $('#main-image');
        let newSrc = $(this).attr('src');
        mainImage.fadeOut(200, function() {
            mainImage.attr('src', newSrc);
            mainImage.fadeIn(200);
        });
    });
}

// ============================================
// Form Validation
// ============================================

function initFormValidation() {
    $('#registration-form').submit(function(e) {
        let password = $('#password').val();
        let confirmPassword = $('#confirm-password').val();
        
        if (password !== confirmPassword) {
            e.preventDefault();
            showNotification('Passwords do not match!', 'error');
            return false;
        }
        
        if (password.length < 6) {
            e.preventDefault();
            showNotification('Password must be at least 6 characters!', 'error');
            return false;
        }
        
        return true;
    });
}

// ============================================
// Checkout Functions
// ============================================

function validateCheckout() {
    let isValid = true;
    
    // Validate address
    if (!$('#address-select').val()) {
        showNotification('Please select a shipping address', 'error');
        isValid = false;
    }
    
    // Validate payment method
    if (!$('input[name="payment-method"]:checked').val()) {
        showNotification('Please select a payment method', 'error');
        isValid = false;
    }
    
    return isValid;
}

function applyCoupon() {
    let couponCode = $('#coupon-code').val();
    
    $.ajax({
        url: '/cart/apply-coupon',
        method: 'POST',
        data: { code: couponCode },
        success: function(response) {
            if (response.valid) {
                showNotification('Coupon applied! Discount: $' + response.discount, 'success');
                updateCartTotal();
                $('#coupon-input').hide();
                $('#coupon-applied').show();
            } else {
                showNotification('Invalid or expired coupon', 'error');
            }
        }
    });
}

// ============================================
// Product Review Functions
// ============================================

function submitReview() {
    let rating = $('input[name="rating"]:checked').val();
    let title = $('#review-title').val();
    let comment = $('#review-comment').val();
    let productId = $('#product-id').val();
    
    if (!rating || !comment) {
        showNotification('Please provide rating and comment', 'error');
        return;
    }
    
    $.ajax({
        url: '/product/' + productId + '/review',
        method: 'POST',
        data: {
            rating: rating,
            title: title,
            comment: comment
        },
        success: function() {
            showNotification('Review submitted successfully!', 'success');
            location.reload();
        },
        error: function() {
            showNotification('Error submitting review', 'error');
        }
    });
}

// ============================================
// Notification System
// ============================================

function showNotification(message, type) {
    let bgColor = type === 'success' ? '#28a745' : 
                  type === 'error' ? '#dc3545' : 
                  type === 'warning' ? '#ffc107' : '#17a2b8';
    
    let icon = type === 'success' ? 'fa-check-circle' :
               type === 'error' ? 'fa-exclamation-circle' :
               type === 'warning' ? 'fa-exclamation-triangle' : 'fa-info-circle';
    
    let notification = `
        <div class="toast-notification" style="position: fixed; bottom: 20px; right: 20px; z-index: 9999;">
            <div class="toast show" role="alert" style="min-width: 300px;">
                <div class="toast-header" style="background: ${bgColor}; color: white;">
                    <i class="fas ${icon} me-2"></i>
                    <strong class="me-auto">Notification</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
                </div>
                <div class="toast-body">
                    ${message}
                </div>
            </div>
        </div>
    `;
    
    $('body').append(notification);
    
    setTimeout(function() {
        $('.toast-notification').fadeOut(300, function() {
            $(this).remove();
        });
    }, 3000);
}

// ============================================
// Loading Spinner
// ============================================

function showLoading() {
    if ($('#loading-spinner').length === 0) {
        $('body').append('<div id="loading-spinner" class="spinner-overlay"><div class="spinner"></div></div>');
    }
    $('#loading-spinner').fadeIn(200);
}

function hideLoading() {
    $('#loading-spinner').fadeOut(200);
}

// ============================================
// Search Auto-complete
// ============================================

function initSearchAutocomplete() {
    $('#search-input').autocomplete({
        source: function(request, response) {
            $.ajax({
                url: '/api/products/search',
                data: { keyword: request.term },
                success: function(data) {
                    response($.map(data, function(item) {
                        return {
                            label: item.name,
                            value: item.name,
                            id: item.id
                        };
                    }));
                }
            });
        },
        select: function(event, ui) {
            window.location.href = '/product/' + ui.item.id;
        }
    });
}

// ============================================
// Price Slider
// ============================================

function initPriceSlider() {
    $( "#price-slider" ).slider({
        range: true,
        min: 0,
        max: 1000,
        values: [ 0, 1000 ],
        slide: function( event, ui ) {
            $( "#price-amount" ).val( "$" + ui.values[ 0 ] + " - $" + ui.values[ 1 ] );
            $( "#min-price" ).val(ui.values[ 0]);
            $( "#max-price" ).val(ui.values[ 1]);
        }
    });
    $( "#price-amount" ).val( "$" + $( "#price-slider" ).slider( "values", 0 ) +
        " - $" + $( "#price-slider" ).slider( "values", 1 ) );
}

// ============================================
// Tooltips Initialization
// ============================================

function initTooltips() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// ============================================
// Lazy Loading Images
// ============================================

function initLazyLoading() {
    if ('IntersectionObserver' in window) {
        let lazyImages = document.querySelectorAll('img.lazy');
        
        let imageObserver = new IntersectionObserver(function(entries, observer) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    let img = entry.target;
                    img.src = img.dataset.src;
                    img.classList.remove('lazy');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        lazyImages.forEach(function(img) {
            imageObserver.observe(img);
        });
    }
}

// ============================================
// Back to Top Button
// ============================================

$(window).scroll(function() {
    if ($(this).scrollTop() > 300) {
        $('#back-to-top').fadeIn();
    } else {
        $('#back-to-top').fadeOut();
    }
});

$('#back-to-top').click(function() {
    $('html, body').animate({scrollTop: 0}, 500);
    return false;
});

// ============================================
// Order Tracking
// ============================================

function trackOrder() {
    let orderNumber = $('#order-number').val();
    
    if (!orderNumber) {
        showNotification('Please enter order number', 'error');
        return;
    }
    
    window.location.href = '/orders/track/' + orderNumber;
}

// ============================================
// Export Functions (for use in other files)
// ============================================

window.ShopSphere = {
    showNotification: showNotification,
    showLoading: showLoading,
    hideLoading: hideLoading,
    updateCartCount: updateCartCount,
    submitReview: submitReview,
    applyCoupon: applyCoupon,
    trackOrder: trackOrder
};