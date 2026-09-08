package com.bookstore.wishlist.dto;

import com.bookstore.product.dto.ProductSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** UC2.3.1/2/3 - flat DTO so Jackson never has to touch the lazy User/Product proxies on
 *  WishlistItem directly (that was throwing a ByteBuddyInterceptor serialization error). */
@Getter
@AllArgsConstructor
public class WishlistItemDto {
    private Long id;
    private ProductSummaryDto product;
}
