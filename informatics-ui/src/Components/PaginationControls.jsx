import Box from '@mui/material/Box'
import MuiPagination from '@mui/material/Pagination'
import Select from '@mui/material/Select'
import MenuItem from '@mui/material/MenuItem'
import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'
import { PAGE_SIZE_OPTIONS } from '../utils/usePagination'

/**
 * The page-size selector + page control every paginated list page (submissions, standings,
 * archive/contest tasks) shares, so they look and behave the same way. Pass the object returned
 * by usePagination() straight through as `pagination`.
 */
export default function PaginationControls({ pagination, pageSizeOptions = PAGE_SIZE_OPTIONS }) {
	const { page, pageSize, totalCount, totalPages, handlePageChange, handlePageSizeChange } = pagination

	if (totalCount === 0) {
		return null
	}

	return (
		<Box
			sx={{
				display: 'flex',
				justifyContent: 'center',
				alignItems: 'center',
				flexWrap: 'wrap',
				gap: 2,
				mt: 2,
				mb: 1,
			}}
		>
			<MuiPagination
				count={totalPages}
				page={page + 1}
				onChange={handlePageChange}
				color="primary"
				showFirstButton
				showLastButton
			/>
			<FormControl size="small" sx={{ minWidth: 120 }}>
				<InputLabel>გვერდის ზომა</InputLabel>
				<Select value={pageSize} label="გვერდის ზომა" onChange={handlePageSizeChange}>
					{pageSizeOptions.map((size) => (
						<MenuItem key={size} value={size}>
							{size}
						</MenuItem>
					))}
				</Select>
			</FormControl>
		</Box>
	)
}
