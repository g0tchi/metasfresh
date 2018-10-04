package de.metas.handlingunits.picking;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;

import de.metas.lang.RepoIdAware;
import de.metas.util.Check;
import lombok.NonNull;
import lombok.Value;

/*
 * #%L
 * de.metas.handlingunits.base
 * %%
 * Copyright (C) 2018 metas GmbH
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

@Value
public class PickingCandidateId implements RepoIdAware
{
	@JsonCreator
	public static PickingCandidateId ofRepoId(final int repoId)
	{
		return new PickingCandidateId(repoId);
	}

	public static PickingCandidateId ofRepoIdOrNull(final int repoId)
	{
		return repoId > 0 ? ofRepoId(repoId) : null;
	}

	public static ImmutableSet<Integer> toIntSet(@NonNull final Collection<PickingCandidateId> ids)
	{
		if (ids.isEmpty())
		{
			return ImmutableSet.of();
		}

		return ids.stream().map(PickingCandidateId::getRepoId).collect(ImmutableSet.toImmutableSet());
	}

	int repoId;

	private PickingCandidateId(final int pickingCandidateRepoId)
	{
		this.repoId = Check.assumeGreaterThanZero(pickingCandidateRepoId, "pickingCandidateRepoId");
	}

	@Override
	@JsonValue
	public int getRepoId()
	{
		return repoId;
	}
}