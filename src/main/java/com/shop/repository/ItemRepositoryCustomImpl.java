package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import com.shop.dto.MainItemDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.QItemImg;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

	private JPAQueryFactory queryFactory;

	public ItemRepositoryCustomImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
		return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
	}

	private BooleanExpression regDtsAfter(String searchDateType) {
		LocalDateTime dateTime = LocalDateTime.now();

		if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
			return null;
		} else if (StringUtils.equals("1d", dateTime)) {
			dateTime = dateTime.minusDays(1);
		} else if (StringUtils.equals("1w", searchDateType)) {
			dateTime = dateTime.minusWeeks(1);
		} else if (StringUtils.equals("1m", searchDateType)) {
			dateTime = dateTime.minusMonths(1);
		} else if (StringUtils.equals("6m", searchDateType)) {
			dateTime = dateTime.minusMonths(6);
		}
		return QItem.item.regTime.after(dateTime);
	}

	private BooleanExpression searchByLike(String searchBy, String searchQuery) {

		if (StringUtils.equals("itemNm", searchBy)) {
			return QItem.item.itemNm.like("%" + searchQuery + "%");
		} else if (StringUtils.equals("createdBy", searchBy)) {
			return QItem.item.createdBy.like("%" + searchQuery + "%");
		}
		return null;
	}

	@Override
	public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

		QueryResults<Item> results = queryFactory // queryFactory를 이용해서 쿼리를 생성한다.
				.selectFrom(QItem.item) // 상품 데이터를 조회하기 위해서 QItem의 item을 지정한다.
				.where(regDtsAfter(itemSearchDto.getSearchDateType()), // BooleanExpression 반환하는 조건문들을 넣어준다.
						searchSellStatusEq(itemSearchDto.getSearchSellStatus()), // 컴마(,)단위로 넣어줄 경우 and조건으로 인식한다.
						searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
				.orderBy(QItem.item.id.desc()).offset(pageable.getOffset()) // 데이터를 가지고 올 시작 인덱스를 지정한다.
				.limit(pageable.getPageSize()) // 한번에 가지고 올 최대 개수를 지정한다.
				.fetchResults(); // 조회한 리스트 및 전체 개수를 포함하는 QueryResult를 반환한다. 상품 데이터 리스트 조회 및 상품 데이터 전체 개수를
									// 조회하는 2번의 쿼리문이 실행된다.

		List<Item> content = results.getResults();
		long total = results.getTotal();

		return new PageImpl<>(content, pageable, total);
	}

	private BooleanExpression itemNmLike(String searchQuery) { // 검색어가 null이 아니면 상품명에 해당 검색어가 포함되는 상품을 조회하는 조건을 반환한다.
		return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
	}

	@Override
	public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
		QItem item = QItem.item;
		QItemImg itemImg = QItemImg.itemImg;
		QueryResults<MainItemDto> results = queryFactory.select(new QMainItemDto( // QMainItemDto의 생성자에 반환할 값들을 넣어준다.
																					// @Projection을 사용하면 DTO로 바로 조회가
																					// 가능하다.
				item.id, // 엔티티 조회 후 DTO로 변환하는 과정을 줄일 수 있다.
				item.itemNm, item.itemDetail, itemImg.imgUrl, item.price)).from(itemImg).join(itemImg.item, item) // itemImg와
																													// item을
																													// 내부
																													// 조인한다.
				.where(itemImg.repimgYn.eq("Y")) // 상품 이미지의 경우 대표 상품 이미지만 불러온다.
				.where(itemNmLike(itemSearchDto.getSearchQuery())).orderBy(item.id.desc()).offset(pageable.getOffset())
				.limit(pageable.getPageSize()).fetchResults();
		List<MainItemDto> content = results.getResults();
		long total = results.getTotal();
		return new PageImpl<>(content, pageable, total);
	}

}
