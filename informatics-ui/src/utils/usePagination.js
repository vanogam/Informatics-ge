import { useState } from 'react'

export const PAGE_SIZE_OPTIONS = [20, 50, 100, 200]

/**
 * Shared page/pageSize/offset bookkeeping for a list backed by a paginated endpoint. The caller
 * still owns fetching - this only tracks "where are we" and derives the offset/totalPages that
 * feed the request params and the <PaginationControls/> UI.
 */
export function usePagination(initialPageSize = 20) {
	const [page, setPage] = useState(0) // 0-indexed, to line up with offset math
	const [pageSize, setPageSize] = useState(initialPageSize)
	const [totalCount, setTotalCount] = useState(0)

	const offset = page * pageSize
	const totalPages = Math.max(1, Math.ceil(totalCount / pageSize))

	const handlePageChange = (event, newPage) => {
		setPage(newPage - 1) // Material-UI Pagination is 1-indexed
	}

	const handlePageSizeChange = (event) => {
		setPageSize(event.target.value)
		setPage(0)
	}

	const resetPage = () => setPage(0)

	return {
		page,
		pageSize,
		totalCount,
		offset,
		totalPages,
		setPage,
		setPageSize,
		setTotalCount,
		handlePageChange,
		handlePageSizeChange,
		resetPage,
	}
}
