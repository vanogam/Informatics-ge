import {
	Button,
	MenuItem,
	Paper,
	Stack,
	TextField,
	Typography,
} from '@mui/material'
import { useState, useRef } from 'react'

export default function NewTestCaseCard({ handleTestCaseSubmit }) {
	const supportedLanguages = ['EN', 'KA']
	const [selectedLanguage, setSelectedLanguage] = useState('EN')
	const [fileName, setFileName] = useState()
	const testCaseRef = useRef(null)

	const handleNewTestCase = () => {
		handleTestCaseSubmit({
			testCaseFile: testCaseRef.current.files[0],
			selectedLanguage,
		})
	}

	const handleFileUpload = () => {
		setFileName(testCaseRef.current.files[0].name)
	}
	return (
		<Paper elevation={4} sx={{ padding: '1rem' }}>
			<Typography align="center" variant="h5" mb="1rem">
				New Test Case
			</Typography>
			<Stack direction="row" gap="1rem">
				<Button fullWidth variant="contained" component="label">
					{fileName ? fileName : 'Upload Test Case'}
					<input
						ref={testCaseRef}
						onChange={handleFileUpload}
						type="file"
						hidden
					/>
				</Button>
				<TextField
					select
					value={selectedLanguage}
					onChange={(e) => {
						setSelectedLanguage(e.target.value)
					}}
					sx={{ minWidth: 'max-content' }}
				>
					{supportedLanguages.map((option) => (
						<MenuItem key={option} value={option}>
							{option}
						</MenuItem>
					))}
				</TextField>
			</Stack>
			<Button
				fullWidth
				variant="contained"
				color="success"
				size="large"
				sx={{ marginTop: '1rem' }}
				onClick={handleNewTestCase}
			>
				Add Test Case
			</Button>
		</Paper>
	)
}