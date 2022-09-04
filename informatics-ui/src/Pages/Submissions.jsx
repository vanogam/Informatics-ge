import { useState } from "react";
import Editor from "react-simple-code-editor";
import { highlight, languages } from "prismjs/components/prism-core";
import axios from "axios";

import Box from "@mui/material/Box";
import Modal from "@mui/material/Modal";
import Table from "@mui/material/Table";
import Paper from "@mui/material/Paper";
import TableRow from "@mui/material/TableRow";
import TableHead from "@mui/material/TableHead";
import TableCell from "@mui/material/TableCell";
import TableBody from "@mui/material/TableBody";
import Typography from "@mui/material/Typography";
import TableContainer from "@mui/material/TableContainer";
import { useParams } from "react-router-dom";
export default function Submissions() {
  const [submissions, setSubmissions] = useState([]);
  const [selectedSubmission, setSelectedSubmission] = useState({});
  const [popUp, setPopUp] = useState(false);
  const { contest_id } = useParams();
//   const something = {
//     message: null,
//     status: "SUCCESS",
//     submissions: [
//       {
//         id: 466,
//         cmsId: 12,
//         userId: 38,
//         username: "datiko",
//         text: "   #include <bits/stdc++.h>\n\nusing namespace std;\n\nint n;\nint cnt[200];\nbool blocked[200];\nstring s;\n\nchar get_min(int i){\n    int mx = n+1;\n    char ret;\n    for(char c='a'; c<='z'; c++){\n        if(blocked[c])\n            continue;\n        if(cnt[c] < mx)\n        {\n            mx = cnt[c];\n            ret = c;\n        }\n    }\n    return ret;\n}\n\nvoid repl(int i){\n    for(char c='a'; c<='z'; c++){\n        blocked[c] = false;\n    }\n    if(i-1>=0) blocked[s[i-1]] = true;\n    if(i-2>=0) blocked[s[i-2]] = true;\n    blocked[s[i]] = true;\n    if(i+1<n) blocked[s[i+1]] = true;\n    if(i+2<n) blocked[s[i+2]] = true;\n    cnt[s[i]]--;\n    s[i] = get_min(i);\n    cnt[s[i]]++;\n}\n\nmain(){\n\n    cin>>s;\n    n=s.size();\n\n    for(int i=0;i<n;i++){\n        cnt[s[i]]++;\n    }\n\n    for(int i=2;i<n;i++){\n        if(s[i]==s[i-1] && s[i]==s[i-2])\n        {\n            repl(i);\n        }\n    }\n\n    for(char c='a'; c<='z'; c++){\n        if(cnt[c] > n/2)\n        {\n            for(int i=0;i<n;i++){\n                if(s[i]==c)\n                    repl(i);\n                if(cnt[c] <= n/2)\n                    break;\n            }\n        }\n    }\n\n    cout<<s<<endl;\n\n}",
//         status: "FINISHED",
//         currentTest: 26,
//         score: 100.0,
//         taskId: 440,
//         contestId: 439,
//         language: "CPP",
//         submissionTime: "2022-09-03T13:52:03.023+00:00",
//         compilationResult: "ok",
//         compilationMessage:
//           "passw.cpp:39:6: warning: ISO C++ forbids declaration of 'main' with no type [-Wreturn-type]\n   39 | main(){\n      |      ^",
//         results: [
//           {
//             idx: "01",
//             outcome: "Correct",
//             text: "Output is correct",
//             time: 0.0,
//             memory: 200704,
//           },
//           {
//             idx: "02",
//             outcome: "Correct",
//             text: "Output is correct",
//             time: 0.0,
//             memory: 208896,
//           },
//           {
//             idx: "03",
//             outcome: "Correct",
//             text: "Output is correct",
//             time: 0.0,
//             memory: 208896,
//           },
//           {
//             idx: "04",
//             outcome: "Correct",
//             text: "Output is correct",
//             time: 0.0,
//             memory: 200704,
//           },
//           {
//             idx: "05",
//             outcome: "Correct",
//             text: "Output is correct",
//             time: 0.0,
//             memory: 200704,
//           },
//         ],
//       },
//     ],
//   };
  const hightlightWithLineNumbers = (input, grammar, language) =>
    highlight(input, grammar, language)
      .split("\n")
      .map((line, i) => `${line}`)
      .join("\n");
  axios
    .get(`http://localhost:8080/contest/${contest_id}/submissions`)
    .then((response) => {
      if (response.data.status === "SUCCESS")
        setSubmissions(response.data.submissions);
      else return <>NO SUBMISSIONS FOUND</>;
    });
  return (
    <>
      <Typography 	sx={{ color: '#452c54', fontWeight: 'bold' }} align="center" variant="h6" mb="1rem" mt="1rem">
        ჩემი მცდელობები
      </Typography>
      <TableContainer
        component={Paper}
        sx={{ maxWidth: "80%", marginInline: "auto" }}
      >
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>Contest / Task</TableCell>
              <TableCell align="right">Username</TableCell>
              <TableCell align="right">Submission Time</TableCell>
              <TableCell align="right">Language</TableCell>
              <TableCell align="right">Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {submissions.map((submission) => (
              <TableRow
                onClick={() => {
                  setSelectedSubmission(submission);
                  setPopUp(true);
                }}
                key={submission.id}
                sx={{
                  "&:last-child td, &:last-child th": { border: 0 },
                  cursor: "pointer",
                  "&:hover": { backgroundColor: "#eee" },
                }}
              >
                <TableCell component="th" scope="row">
                  {submission.contestId} / {submission.taskId}
                </TableCell>
                <TableCell align="right">{submission.username}</TableCell>
                <TableCell align="right">{submission.submissionTime}</TableCell>
                <TableCell align="right">{submission.language}</TableCell>
                <TableCell align="right">{submission.status}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <Modal open={popUp} onClose={() => setPopUp(false)}>
        <Box
          sx={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            maxHeight: "80%",
            overflowY: "auto",
            width: "400px",
            bgcolor: "white",
            border: `2px solid ;`,
            borderRadius: "0.5rem",
            boxShadow: 24,
            p: 4,
          }}
        >
          <Paper elevation={4} sx={{ padding: "1rem", marginBottom: "1rem" }}>
            <Typography sx={{ fontSize: "10px", fontWeight: "400" }}>
              User: {selectedSubmission.username}
            </Typography>
            <Typography sx={{ fontSize: "10px", fontWeight: "400" }}>
              Lang: {selectedSubmission.language}
            </Typography>
            <Typography sx={{ fontSize: "10px", fontWeight: "400" }}>
              Submission Time: {selectedSubmission.submissionTime}
            </Typography>
          </Paper>
          <Paper elevation={4} sx={{ padding: "1rem", marginBottom: "1rem" }}>
            <Editor
              value={selectedSubmission.text}
              highlight={(code) =>
                hightlightWithLineNumbers(code, languages.cpp, "cpp")
              }
              textareaId="codeArea"
              style={{
                overflowY: "auto",
                maxHeight: "20rem",
                overflowY: "auto",
                fontFamily: '"Fira code", "Fira Mono", monospace',
                fontSize: 12,
              }}
            />
          </Paper>
          <Paper elevation={4} sx={{ padding: "1rem", marginBottom: "1rem" }}>
            <Typography sx={{ fontSize: "13px", fontWeight: "400" }}>
              Compilation Result: {selectedSubmission.compilationResult}
              <br />
              Compilation Message: {selectedSubmission.compilationMessage}
            </Typography>
          </Paper>
          {selectedSubmission.compilationResult === "ok" ? (
            selectedSubmission.status === "FINISHED" ? (
              <Paper elevation={4} sx={{ padding: "1rem" }}>
                <Typography align="center" variant="h6" mb="1rem">
                  Test cases
                </Typography>
                {selectedSubmission?.results?.map((testCase) => (
                  <Paper elevation={4} sx={{ padding: "1rem" }}>
                    <Typography sx={{fontSize: "15px"}}>#{testCase.idx}</Typography>
                    <Typography sx={{fontSize: "15px"}} >
                      Status:{" "}
                      <span style={{ fontWeight: 700 }}>
                        {testCase.outcome}
                      </span>
                    </Typography>
                    <Typography sx={{fontSize: "15px"}}>
                      Text:{" "}
                      <span style={{ fontWeight: 700 }}>{testCase.text}</span>
                    </Typography>
                    <Typography sx={{fontSize: "15px"}}>
                      Time:{" "}
                      <span style={{ fontWeight: 700 }}>{testCase.time}</span>
                    </Typography>
                    <Typography sx={{fontSize: "15px"}}>
                      Memory:{" "}
                      <span style={{ fontWeight: 700 }}>{testCase.memory}</span>
                    </Typography>
                  </Paper>
                ))}
              </Paper>
            ) : (
              <Paper elevation={4} sx={{ padding: "1rem" }}>
                <Typography>
                  Running on test: {selectedSubmission.currentTest}
                </Typography>
              </Paper>
            )
          ) : (
            <Paper elevation={4} sx={{ padding: "1rem" }}>
              <Typography>Error Compiling</Typography>
            </Paper>
          )}
        </Box>
      </Modal>
    </>
  );
}
