package com.shop.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.modelmapper.ModelMapper;

import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemFormDto {

	private Long id;
	
	@NotBlank(message = "상품명은 필수 입력 값입니다.")
	private String itemNm;
	
	@NotNull(message = "가격은 필수 입력 값입니다.")
	private Integer price;
	
	@NotBlank(message = "상품 상세는 필수 입력 값입니다.")
	private String itemDetail;
	
	@NotNull(message = "재고는 필수 입력 값입니다.")
	private Integer stockNumber;
	
	private ItemSellStatus itemSellStatus;	// 상품 판매상태
	
	private List<ItemImgDto> itemImgDtoList = new ArrayList<>();	// 상품 저장 후 수정할 때 상품 이미지 정보를 저장하는 리스트
	
	private List<Long> itemImgIds = new ArrayList<>();
	
	private static ModelMapper modelMapper = new ModelMapper();
	
	public Item createdItem() {
		return modelMapper.map(this, Item.class);
	}
	
	public static ItemFormDto of(Item item) {
		return modelMapper.map(item, ItemFormDto.class);
	}
}
