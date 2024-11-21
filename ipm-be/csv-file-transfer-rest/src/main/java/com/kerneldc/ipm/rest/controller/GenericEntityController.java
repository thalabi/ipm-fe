package com.kerneldc.ipm.rest.controller;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Enums;
import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.common.search.EntitySpecification;
import com.kerneldc.ipm.commonservices.repository.EntityRepositoryFactory;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/genericEntityController")
@RequiredArgsConstructor
@Slf4j
public class GenericEntityController {

	private final EntityRepositoryFactory<?, ?> entityRepositoryFactory;
	private final EntityRepresentationModelAssembler entityRepresentationModelAssembler;
	private final EntityManager em;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/findAll")
	public ResponseEntity<PagedModel<AbstractEntity>> findAll(
			@RequestParam @NotNull String tableName, @RequestParam String search,
			Pageable pageable, PagedResourcesAssembler<AbstractEntity> pagedResourcesAssembler) {

		Objects.requireNonNull(pagedResourcesAssembler, "pagedResourcesAssembler cannot be null for this controller to work");
    	
		if (StringUtils.isBlank(tableName)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
    	
		IEntityEnum entityEnum = getEntityEnum(tableName);
    	LOGGER.info("search: {}", search);
    	LOGGER.info("pageable: {}", pageable);
    	
    	var entityRepository = entityRepositoryFactory.getRepository(entityEnum);
    	var entityMetamodel = em.getMetamodel().entity(entityEnum.getEntity());
    	
    	var entitySpecification = new EntitySpecification<AbstractEntity>(entityMetamodel, search);
    	
		var page = entityRepository.findAll((Specification)entitySpecification, pageable);
        PagedModel<?> pagedModel; 
        if (! /* not */ page.hasContent()) {
        	pagedModel = pagedResourcesAssembler.toEmptyModel(page, entityEnum.getEntity());
        } else {
        	var link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GenericEntityController.class).findAll(tableName, search, pageable, pagedResourcesAssembler)).withSelfRel();
        	pagedModel = pagedResourcesAssembler.toModel(page, entityRepresentationModelAssembler, link);
        }

		LOGGER.debug("pagedModel: {}", pagedModel);
		
		var response = ResponseEntity.ok((PagedModel<AbstractEntity>)pagedModel);
		
		LOGGER.debug("r: {}", response);
		return response;	
    }
//	@SuppressWarnings("rawtypes")
//	@GetMapping("/findAll")
//	public ResponseEntity<PagedModel<AbstractEntity>> findAll(
//			@RequestParam @NotNull String tableName, @RequestParam String search,
//			Pageable pageable, PagedResourcesAssembler<AbstractEntity> pagedResourcesAssembler) {
//
//		Objects.requireNonNull(pagedResourcesAssembler, "pagedResourcesAssembler cannot be null for this controller to work");
//    	
//		if (StringUtils.isBlank(tableName)) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//		}
//    	
//		IEntityEnum tEnum = getEntityEnum(tableName);
//    	LOGGER.info("search: {}", search);
//    	LOGGER.info("pageable: {}", pageable);
//    	
//    	var searchCriteriaList = SearchCriteria.searchStringToSearchCriteriaList(search);
//    	var entitySpecificationsBuilder2 = new EntitySpecificationsBuilder<AbstractEntity>();
//    	var sunshineListRep = entityRepositoryFactory.getRepository(tEnum);
//
//        @SuppressWarnings("unchecked")
//		var entityPage2 = sunshineListRep.findAll((Specification)entitySpecificationsBuilder2.with(searchCriteriaList).build(), pageable);
//		var link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GenericEntityController.class).findAll(tableName, search, pageable, pagedResourcesAssembler)).withSelfRel();
//		@SuppressWarnings("unchecked")
//		var entityPagedResources = pagedResourcesAssembler.toModel(entityPage2, entityRepresentationModelAssembler, link);
//		
//		LOGGER.debug("entityPagedResources: {}", entityPagedResources);
//		
//		@SuppressWarnings("unchecked")
//		ResponseEntity<PagedModel<AbstractEntity>> response = ResponseEntity.ok(entityPagedResources);
//		
//		LOGGER.debug("r: {}", response);
//		return response;
//    }

	private IEntityEnum getEntityEnum(String tableName) {
		IEntityEnum tEnum;
		if (Enums.getIfPresent(UploadTableEnum.class, tableName.toUpperCase()).isPresent()) {
			tEnum = UploadTableEnum.valueOf(tableName.toUpperCase());
		} else {
			tEnum = InvestmentPortfolioEntityEnum.valueOf(tableName.toUpperCase());
		}
		return tEnum;
	}
//	@GetMapping("/findAll")
//	public ResponseEntity<PagedModel<AbstracteEntity>> findAll(
//			@RequestParam @NotNull String tableName, @RequestParam String search,
//			Pageable pageable, PagedResourcesAssembler<AbstractPersistableEntity> pagedResourcesAssembler) {
//
//		Objects.requireNonNull(pagedResourcesAssembler, "pagedResourcesAssembler cannot be null for this controller to work");
//    	
//		if (StringUtils.isBlank(tableName)) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//		}
//    	
//    	var tEnum = UploadTableEnum.valueOf(tableName.toUpperCase());
//    	LOGGER.info("search: {}", search);
//    	LOGGER.info("pageable: {}", pageable);
//    	
//    	var searchCriteriaList = SearchCriteria.searchStringToSearchCriteriaList(search);
//    	var entitySpecificationsBuilder2 = new EntitySpecificationsBuilder<AbstractPersistableEntity>();
//    	var sunshineListRep = entityRepositoryFactory.getRepository(tEnum);
//
//        @SuppressWarnings("unchecked")
//		var entityPage2 = sunshineListRep.findAll((Specification)entitySpecificationsBuilder2.with(searchCriteriaList).build(), pageable);
//		var link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GenericEntityController.class).findAll(tableName, search, pageable, pagedResourcesAssembler)).withSelfRel();
//		@SuppressWarnings("unchecked")
//		var entityPagedResources = pagedResourcesAssembler.toModel(entityPage2, entityRepresentationModelAssembler, link);
//		
//		LOGGER.debug("entityPagedResources: {}", entityPagedResources);
//		
//		@SuppressWarnings("unchecked")
//		ResponseEntity<PagedModel<AbstractPersistableEntity>> response = ResponseEntity.ok(entityPagedResources);
//		
//		LOGGER.debug("r: {}", response);
//		return response;
//    }
}
