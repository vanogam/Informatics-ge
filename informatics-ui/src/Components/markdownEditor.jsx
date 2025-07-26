import React, {useContext, useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";
import rehypeRaw from "rehype-raw";
import getMessage from "./lang";
import {AxiosContext} from '../utils/axiosInstance'
import {Button} from "@mui/material";
import {youtubeRegex} from "../utils/constants";

const MarkdownEditor = ({
                            entries,
                            loadEndpoint,
                            value,
                            onChange,
                            loading = false,
                            comment = null,
                            saveText,
                            imageDownloadFunc,
                            imageUploadAddress,
                            submitFunc
                        }) => {
    const [activeTab, setActiveTab] = useState("editor");
    const axiosInstance = useContext(AxiosContext)

    const loadData = () => {
        axiosInstance.get(loadEndpoint)
            .then(response => {
                if (response.status === 200) {
                    onChange(response.data.statement || "");
                }
            })
    }

    useEffect(() => {
        loadEndpoint && loadData()
    }, [])

    const handlePaste = async (e, onChange) => {
        const items = e.clipboardData.items;
        for (const item of items) {
            if (item.type.startsWith("image/")) {
                const file = item.getAsFile();
                const formData = new FormData();
                formData.append("file", file);

                axiosInstance.post(imageUploadAddress, formData).then((response) => {
                    const imageUrl = response.data.imageUrl;

                    if (response.status === 200) {
                        const imageMarkdown = `![Image](${imageUrl})\n`;
                        onChange((prev) => prev + imageMarkdown);
                    } else {
                        console.error("Image upload failed:", response.statusText);
                    }
                });
                e.preventDefault();
            } else if (item.type === "text/plain") {
                const text = e.clipboardData.getData("text/plain");
                const match = youtubeRegex.exec(text);

                if (match) {
                    const videoId = match[2] || match[3];
                    const embeddedVideoMarkdown = `<iframe width="560" height="315" src="https://www.youtube.com/embed/${videoId}" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>\n`;
                    onChange((prev) => prev + embeddedVideoMarkdown);
                    e.preventDefault();
                }
            }
        }
    };

    return (
        <div>
            <div style={{display: "flex", borderBottom: "1px solid #ccc"}}>
                <button
                    style={{
                        flex: 1,
                        padding: "10px",
                        cursor: "pointer",
                        background: activeTab === "editor" ? "#f0f0f0" : "white",
                        border: "none",
                        borderBottom: activeTab === "editor" ? "2px solid blue" : "none",
                    }}
                    onClick={() => setActiveTab("editor")}
                >
                    {getMessage('ka', 'editor')}
                </button>
                <button
                    style={{
                        flex: 1,
                        padding: "10px",
                        cursor: "pointer",
                        background: activeTab === "preview" ? "#f0f0f0" : "white",
                        border: "none",
                        borderBottom: activeTab === "preview" ? "2px solid blue" : "none",
                    }}
                    onClick={() => setActiveTab("preview")}
                >
                    {getMessage('ka', 'preview')}
                </button>
            </div>
            <div style={{paddingTop: "10px"}}>
                {activeTab === "editor" && entries.map((entry) => (
                    <>
                        <label key={entry.label} style={{display: "block", marginBottom: "10px"}}>{entry.label}</label>
                        <textarea
                            style={{width: "100%", height: entry.height, fontSize:"16px" }}
                            placeholder={getMessage('ka', 'markdownPlaceholder')}
                            value={entry.value}
                            onChange={e => entry.onChange(e.target.value)}
                            onPaste={e => handlePaste(e, entry.onChange)}
                        />
                    </>))
                }
                {activeTab === "preview" &&
                    <div style={{border: "1px solid #ccc", padding: "10px"}}>

                        {entries.map((entry) => (
                            <>
                                {entry.labelVisible &&
                                <p key={entry.label}
                                   style={{display: "block", marginBottom: "10px", fontWeight: "bold"}}>{entry.label}</p>
                                }
                                <ReactMarkdown
                                    children={entry.value}
                                    remarkPlugins={[remarkMath]}
                                    rehypePlugins={[rehypeMathjax, rehypeRaw]}
                                    urlTransform={imageDownloadFunc}
                                />
                            </>
                        ))}
                    </div>
                }
            </div>
            <div style={{marginBottom: "10px", fontStyle: "italic", color: 'gray', fontSize: '15px', minHeight: '20px'}}>{comment}</div>            <div style={{marginLeft: 'auto'}}>
                <Button
                    variant="contained"
                    color="secondary"
                    sx={{backgroundColor: '#2f2d47'}}
                    disabled={loading}
                    onClick={
                        () => {
                            submitFunc(value)
                        }
                    }
                >
                    {saveText || getMessage('ka', 'save')}
                </Button>
            </div>
        </div>
    );
};

export default MarkdownEditor;