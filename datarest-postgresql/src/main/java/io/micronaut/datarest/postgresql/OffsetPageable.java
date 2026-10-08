/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.datarest.postgresql;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;

/**
 * A {@link Pageable} addressed by row offset rather than page number, as PostgREST {@code limit} and
 * {@code offset} parameters are. Micronaut Data's own pageables derive the offset from the page number,
 * which cannot express an offset that is not a multiple of the page size.
 *
 * @param offset number of rows skipped
 * @param size   maximum number of rows
 * @param sort   sort order
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
public record OffsetPageable(long offset, int size, Sort sort) implements Pageable {

    @Override
    public int getNumber() {
        return (int) (offset / size);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageable(offset + size, size, sort);
    }

    @Override
    public Pageable previous() {
        return new OffsetPageable(Math.max(0, offset - size), size, sort);
    }

    @Override
    public Pageable withSort(Sort sort) {
        return new OffsetPageable(offset, size, sort);
    }

    @Override
    public Pageable withoutSort() {
        return withSort(Sort.unsorted());
    }

    @Override
    public Pageable withoutPaging() {
        return Pageable.from(sort);
    }

    @Override
    public Pageable withTotal() {
        return this;
    }

    @Override
    public Pageable withoutTotal() {
        return this;
    }
}
