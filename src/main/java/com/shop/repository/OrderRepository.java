package com.shop.repository;

import com.shop.entity.Order;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Query("select o from Order o " + "where o.member.email = :email " + "order by o.orderDate desc")
	List<Order> findOrders(@Param("email") String email, Pageable pageable); // 현재 로그인한 사용자 주문 데이터 페이징 조건에 맞춰 조회

	@Query("select count(o) from Order o " + "where o.member.email = :email")
	Long countOrder(@Param("email") String email); // 현재 로그인한 회원 주문 개수가 몇 개인지 조회한다.
}
