import { Paper, Stack, TextField, Typography } from '@mui/material'
import { useRef } from 'react'

export default function NewTaskCard() {
	const codeRef = useRef(null)
	const titleENref = useRef(null)
	const titleKAref = useRef(null)
	const taskTypeRef = useRef(null)
	const taskScoreTypeRef = useRef(null)
	const taskScoreParameterRef = useRef(null)
	const timeLimitMillisRef = useRef(null)
	const memoryLimitMBref = useRef(null)
	const inputTemplateRef = useRef(null)
	const outputTemplateRef = useRef(null)

	return (
		<Paper elevation={4} sx={{ padding: '1rem' }}>
			<Typography align="center" variant="h4" mb="1rem">
				Add Task
			</Typography>
			<Stack gap="1rem" maxWidth="25rem" mx="auto">
				<TextField label="Code" inputRef={codeRef} variant="outlined" />
				<Stack flexDirection="row" gap="1rem">
					<TextField
						label="Title (ka)"
						inputRef={titleENref}
						variant="outlined"
					/>
					<TextField
						label="Title (en)"
						inputRef={titleKAref}
						variant="outlined"
					/>
				</Stack>
				{/* <TextField type="file" variant="outlined" /> */}
				<TextField label="taskType" inputRef={taskTypeRef} variant="outlined" />
				<TextField
					label="taskScoreType"
					inputRef={taskScoreTypeRef}
					variant="outlined"
				/>
				<TextField
					label="taskScoreParameter"
					inputRef={taskScoreParameterRef}
					variant="outlined"
				/>
				<TextField
					label="timeLimitMillis"
					inputRef={timeLimitMillisRef}
					variant="outlined"
				/>
				<TextField
					label="memoryLimitMB"
					inputRef={memoryLimitMBref}
					variant="outlined"
				/>
				<TextField
					label="inputTemplate"
					inputRef={inputTemplateRef}
					variant="outlined"
				/>
				<TextField
					label="outputTemplate"
					inputRef={outputTemplateRef}
					variant="outlined"
				/>
			</Stack>{' '}
			<Paper elevation={4} sx={{ padding: '1rem' }}></Paper>
		</Paper>
	)
}