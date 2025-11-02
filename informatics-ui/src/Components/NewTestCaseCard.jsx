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
	const [fileName, setFileName] = useState()
	const testcaseRef = useRef(null)

	const handleNewTestCase = () => {
		handleTestCaseSubmit({
			testcaseFile: testcaseRef.current.files[0],
		})
	}

	const handleFileUpload = () => {
		setFileName(testcaseRef.current.files[0].name)
		handleNewTestCase()
	}
	return (
		<Paper elevation={4} sx={{ padding: '1rem' }}>
			<Typography align="center" variant="h6" mb="1rem">
				ახალი ტესტ-ქეისები
			</Typography>
			<Stack direction="row" gap="1rem">
				<Button fullWidth  variant="contained" component="label">
					{fileName ? fileName : 'ქეისების ატვირთვა'}
					<input
						ref={testcaseRef}
						onChange={handleFileUpload}
						type="file"
						hidden
					/>
				</Button>
				{/*<TextField*/}
				{/*	size = "small"*/}
				{/*	select*/}
				{/*	value={selectedLanguage}*/}
				{/*	onChange={(e) => {*/}
				{/*		setSelectedLanguage(e.target.value)*/}
				{/*	}}*/}
				{/*	sx={{ minWidth: 'max-content' }}*/}
				{/*>*/}
				{/*	{supportedLanguages.map((option) => (*/}
				{/*		<MenuItem key={option} value={option}>*/}
				{/*			{option}*/}
				{/*		</MenuItem>*/}
				{/*	))}*/}
				{/*</TextField>*/}
			</Stack>
			{/*<Button*/}
			{/*	fullWidth*/}
			{/*	variant="contained"*/}
			{/*	size="large"*/}
			{/*	sx={{ marginTop: '1rem' }}*/}
			{/*	onClick={handleNewTestCase}*/}
			{/*>*/}
			{/*	დაამატე ტესტ-ქეისები*/}
			{/*</Button>*/}
		</Paper>
	)
}