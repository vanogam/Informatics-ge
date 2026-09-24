import { Box, Typography } from '@mui/material'

const START_YEAR = 2026

function Footer() {
	const currentYear = new Date().getFullYear()
	const yearLabel =
		currentYear > START_YEAR ? `${START_YEAR}-${currentYear}` : `${START_YEAR}`

	return (
		<Box
			component="footer"
			sx={{
				borderTop: '1px solid rgba(0, 0, 0, 0.12)',
				padding: '1rem 4rem',
				background: '#fff',
				marginTop: 'auto',
			}}
		>
			<Typography
				align="center"
				color="text.secondary"
				sx={{
					fontSize: 14,
				}}
			>
				informatics.ge {yearLabel} all rights reserved
			</Typography>
		</Box>
	)
}

export default Footer
