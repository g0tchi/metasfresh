package de.metas.material.dispo.service.candidatechange;

import static org.adempiere.model.InterfaceWrapperHelper.load;
import static org.adempiere.model.InterfaceWrapperHelper.save;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import de.metas.material.dispo.commons.CandidatesQuery;
import de.metas.material.dispo.commons.CandidatesQuery.CandidatesQueryBuilder;
import de.metas.material.dispo.commons.candidate.Candidate;
import de.metas.material.dispo.commons.candidate.CandidateType;
import de.metas.material.dispo.commons.repository.CandidateRepositoryRetrieval;
import de.metas.material.dispo.commons.repository.CandidateRepositoryWriteService;
import de.metas.material.dispo.model.I_MD_Candidate;
import de.metas.material.event.commons.MaterialDescriptor;
import de.metas.material.event.commons.MaterialDescriptor.DateOperator;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-manufacturing-dispo
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Service
public class StockCandidateService
{
	private final CandidateRepositoryRetrieval candidateRepositoryRetrieval;
	private final CandidateRepositoryWriteService candidateRepositoryCommands;

	public StockCandidateService(
			@NonNull final CandidateRepositoryRetrieval candidateRepository,
			@NonNull final CandidateRepositoryWriteService candidateRepositoryCommands)
	{
		this.candidateRepositoryRetrieval = candidateRepository;
		this.candidateRepositoryCommands = candidateRepositoryCommands;
	}

	/**
	 * Creates and returns <b>but does not store</b> a new stock candidate
	 * whose quantity is the quantity of the given {@code candidate} plus the quantity from
	 * the next-younger (not same-age!) stock candidate that has the same product, storage attributes key and warehouse.
	 *
	 * If there is no such next-younger stock candidate (i.e. if this is the very first stock candidate to be created for the given product and locator), then a quantity of zero is taken.
	 *
	 * @param candidate
	 * @return a candidate with
	 *         <ul>
	 *         <li>type = {@link CandidateType#STOCK}</li>
	 *         <li>qty = qty of the given {@code candidate} plus the next younger candidate's quantity
	 *         <li>groupId of the next younger-candidate (or null if there is none)
	 *         </ul>
	 */
	public Candidate createStockCandidate(@NonNull final Candidate candidate)
	{
		// TODO cleanup when it's fine
		final CandidatesQueryBuilder stockQueryBuilder = candidate.createStockQueryBuilderWithBeforeOperator();
		// if (candidate.getParentId() > 0)
		// {
		// stockQueryBuilder.parentId(candidate.getParentId());
		// }
		final Candidate previousStockOrNull = candidateRepositoryRetrieval
				.retrieveLatestMatchOrNull(stockQueryBuilder
						.parentId(CandidatesQuery.UNSPECIFIED_PARENT_ID)
						.build());

		final BigDecimal previousQuantity = previousStockOrNull != null
				? previousStockOrNull.getQuantity()
				: BigDecimal.ZERO;

		final MaterialDescriptor materialDescriptor = candidate
				.getMaterialDescriptor()
				.withQuantity(previousQuantity.add(candidate.getQuantity()));

//		final Candidate updatableStockOrNull = candidateRepositoryRetrieval
//				.retrieveLatestMatchOrNull(stockQueryBuilder
//						.parentId(candidate.getParentId())
//						.build());

		final Integer groupId = previousStockOrNull != null
				? previousStockOrNull.getGroupId()
				: 0;

//				candidate
//				.withType(CandidateType.STOCK)
//				.withMaterialDescriptor(materialDescriptor);

		return Candidate.builder()
				.type(CandidateType.STOCK)
				.orgId(candidate.getOrgId())
				.clientId(candidate.getClientId())
				.materialDescriptor(materialDescriptor)
				.parentId(candidate.getParentId())
				.seqNo(candidate.getSeqNo())
				.groupId(groupId)
				.build();
	}

	/**
	 * This method creates or updates a stock candidate according to the given candidate.
	 * It then updates the quantities of all "later" stock candidates that have the same product etc.<br>
	 * according to the delta between the candidate's former value (or zero if it was only just added) and it's new value.
	 *
	 * @param relatedCandidate a candidate that can have any type and a quantity which is a <b>delta</b>, i.e. a quantity that is added or removed at the candidate's date.
	 *
	 * @return a candidate with type {@link CandidateType#STOCK} that reflects what was persisted in the DB.<br>
	 *         The candidate will have an ID and its quantity will not be a delta, but the "absolute" projected quantity at the given time.<br>
	 *         Note: this method does not establish a parent-child relationship between any two records
	 */
	public Candidate addOrUpdateStock(@NonNull final Candidate relatedCandidate)
	{
		final Supplier<Candidate> stockCandidateToUpdate = () -> {
			final Candidate stockCandidateToPersist = createStockCandidate(relatedCandidate
					//.withParentId(CandidatesQuery.UNSPECIFIED_PARENT_ID) // TODO cleanup when OK
					);

			final Candidate persistedStockCandidate = candidateRepositoryCommands
					.addOrUpdatePreserveExistingSeqNo(stockCandidateToPersist);

			return persistedStockCandidate;
		};

		return updateStock(
				relatedCandidate,
				stockCandidateToUpdate);
	}

	/**
	 * Updates the qty for the stock candidate returned by the given {@code stockCandidateToUpdate} and it's later stock candidates
	 *
	 * @param relatedCandiateWithDelta
	 * @param stockCandidateToUpdate
	 * @return
	 */
	public Candidate updateStock(
			@NonNull final Candidate relatedCandiateWithDelta,
			@NonNull final Supplier<Candidate> stockCandidateToUpdate)
	{
		final CandidatesQuery query = CandidatesQuery.fromCandidate(relatedCandiateWithDelta, false);

		final Candidate previousCandidateOrNull = candidateRepositoryRetrieval.retrieveLatestMatchOrNull(query);

		final Candidate persistedStockCandidate = stockCandidateToUpdate.get();

		final BigDecimal delta;
		if (previousCandidateOrNull != null)
		{
			// there was already a persisted candidate. This means that the addOrReplace already did the work of providing our delta between the old and the current status.
			delta = persistedStockCandidate.getQuantity();
		}
		else
		{
			// there was no persisted candidate, so we basically propagate the full qty (positive or negative) of the given candidate upwards
			delta = relatedCandiateWithDelta.getQuantity();
		}
		applyDeltaToMatchingLaterStockCandidates(
				relatedCandiateWithDelta.getMaterialDescriptor(),
				persistedStockCandidate.getGroupId(),
				delta);
		return persistedStockCandidate;
	}

	/**
	 * Updates the qty of the given candidate.
	 * Differs from {@link #addOrUpdateOverwriteStoredSeqNo(Candidate)} in that
	 * only the ID of the given {@code candidateToUpdate} is used, and if there is no existing persisted record, then an exception is thrown.
	 * Also it just updates the underlying persisted record of the given {@code candidateToUpdate} and nothing else.
	 *
	 *
	 * @param candidateToUpdate the candidate to update. Needs to have {@link Candidate#getId()} > 0.
	 *
	 * @return a copy of the given {@code candidateToUpdate} with the quantity being a delta, similar to the return value of {@link #addOrUpdate(Candidate, boolean)}.
	 */
	public Candidate updateQty(@NonNull final Candidate candidateToUpdate)
	{
		Preconditions.checkState(candidateToUpdate.getId() > 0,
				"Parameter 'candidateToUpdate' needs to have Id > 0; candidateToUpdate=%s",
				candidateToUpdate);

		final I_MD_Candidate candidateRecord = load(candidateToUpdate.getId(), I_MD_Candidate.class);
		final BigDecimal oldQty = candidateRecord.getQty();

		candidateRecord.setQty(candidateToUpdate.getQuantity());
		save(candidateRecord);

		final BigDecimal qtyDelta = candidateToUpdate.getQuantity().subtract(oldQty);

		return candidateToUpdate.withQuantity(qtyDelta);
	}

	/**
	 * Selects all stock candidates which have the same product and locator but a later timestamp than the one from the given {@code segment}.
	 * Iterate them and add the given {@code delta} to their quantity.
	 * <p>
	 * That's it for now :-). Don't alter those stock candidates' children or parents.
	 *
	 * @param productDescriptor the product to match against
	 * @param warehouseId the warehouse ID to match against
	 * @param date the date to match against (i.e. everything after this date shall be a match)
	 * @param groupId the groupId to set to every stock record that we matched
	 * @param delta the quantity (positive or negative) to add to every stock record that we matched
	 */
	public
	/* package */ void applyDeltaToMatchingLaterStockCandidates(
			@NonNull final MaterialDescriptor materialDescriptor,
//			@NonNull final Integer warehouseId,
//			@NonNull final Date date,
			@NonNull final Integer groupId,
			@NonNull final BigDecimal delta)
	{
		final CandidatesQuery query = CandidatesQuery.builder()
				.type(CandidateType.STOCK)
				.materialDescriptor(MaterialDescriptor.builderForQuery()
						.date(materialDescriptor.getDate())
						.productDescriptor(materialDescriptor)
						.warehouseId(materialDescriptor.getWarehouseId())
						.dateOperator(DateOperator.AFTER)
						.build())
				.parentId(CandidatesQuery.UNSPECIFIED_PARENT_ID)
				.matchExactStorageAttributesKey(true)
				.build();

		final List<Candidate> candidatesToUpdate = candidateRepositoryRetrieval.retrieveOrderedByDateAndSeqNo(query);
		for (final Candidate candidate : candidatesToUpdate)
		{
			final BigDecimal newQty = candidate.getQuantity().add(delta);
			candidateRepositoryCommands.addOrUpdateOverwriteStoredSeqNo(candidate
					.withQuantity(newQty)
					.withGroupId(groupId));
		}
	}
}
