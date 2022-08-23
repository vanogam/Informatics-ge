import React from 'react'
import Box from '@mui/material/Box'
import Editor from 'react-simple-code-editor'
import { highlight, languages } from 'prismjs/components/prism-core'
import 'prismjs/components/prism-clike'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'
import 'prismjs/components/prism-javascript'
import 'prismjs/themes/prism.css' //Example style, you can use another
import './numbers.css'
import { Button } from '@mui/material'
import TextField from '@mui/material/TextField'

const boxStyle = {
	display: 'flex',
	flexDirection: 'column',
	'& .MuiTextField-root': { m: 1, width: '25ch' },
	paddingLeft: '30%',
	marginLeft: '10%',
	paddingTop: '5%',
	color: 'purple',
	paddingLeft: '10%',
	fontWeight: 'bold',

	fontSize: '20',
}
const hightlightWithLineNumbers = (input, grammar, language) =>
	highlight(input, grammar, language)
		.split('\n')
		.map((line, i) => `<span class='editorLineNumber'>${i + 1}</span>${line}`)
		.join('\n')

export default function Materials() {
	const [code, setCode] = React.useState(
		`#include <iostream>\nusing namespace std;\nint main()\n{\ncout << "Hello, World!";\nreturn 0; \n}\n`
	)
	return (
		<Box
			sx={{
				display: 'flex',
				flexDirection: 'row',
			}}
		>
			<Box
				sx={{
					paddingTop: '50px',
					'& .MuiTextField-root': { m: 1, width: '50ch' },

					paddingLeft: '10%',
					display: 'flex',
					flexDirection: 'column',

					fontSize: '20',
				}}
			>
				<p sx={{ color: 'purple' }}>შეიყვანე კოდი: </p>
					<Editor
						value={code}
						onValueChange={(code) => setCode(code)}
						highlight={(code) => hightlightWithLineNumbers(code, languages.cpp, 'cpp')}
						className="editor"
						textareaId="codeArea"
						style={{
							overflowY: 'auto',
							height: '50vh',
							fontFamily: '"Fira code", "Fira Mono", monospace',
							fontSize: 12,
						}}
					/>
				<Button
					sx={{
						marginInline: '2px',
						alignSelf: 'right',
						marginLeft: '60px',
						width: '50%',
						marginTop: '5%',
						background: '#3c324e',
					}}
					variant="contained"
				>
					დაკომპილირება
				</Button>
			</Box>

			<Box sx={boxStyle} component="form">
				<p>შემავალი მონაცემები: </p>
				<TextField
					id="outlined-multiline-static"
					multiline
					rows={5}
				></TextField>

				<Button
					sx={{
						marginInline: '2px',
						alignSelf: 'right',
						width: '50%',
						marginBottom: '20%',
						marginLeft: '30px',
						background: '#3c324e',
					}}
					variant="contained"
				>
					ფაილის ატვირთვა
				</Button>

				<p>გამომავალი მონაცემები: </p>
				<TextField
					id="outlined-multiline-static"
					multiline
					rows={4}
				></TextField>
			</Box>
		</Box>
	)
}
