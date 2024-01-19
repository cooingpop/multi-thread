/*
 * Constants.java 2024-01-16
 *
 * @author junyoung.park
 * Copyright 2024. PlayD Corp. All rights Reserved.
 */
package com.wywta.model;

import lombok.Getter;

public class Constants {
    @Getter
    public enum ReportItem {
        Campaign(300),
        CampaignBudget(600),
        BusinessChannel(300),
        Adgroup(200),
        AdgroupBudget(6000),
        Keyword(100),
        Ad(300),
        ShoppingProduct(600),
        ContentsAd(300),
        CatalogAd(600),
        AdQi(600),
        ProductGroup(600),
        ProductGroupRel(600),
        BrandAd(600),
        BrandThumbnailAd(600),
        BrandBannerAd(600),
        Criterion(600),
        PlaceAd(300);

        private final int value;

        ReportItem(int value) {
            this.value = value;
        }

    }
}
