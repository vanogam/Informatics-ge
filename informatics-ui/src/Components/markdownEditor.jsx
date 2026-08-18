import React, {useContext, useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import "../styles/markdown.css";
import remarkMath from "remark-math";
import rehypeMathjax from "rehype-mathjax";
import rehypeRaw from "rehype-raw";
import getMessage from "./lang";
import {AxiosContext} from '../utils/axiosInstance'
import {Button} from "@mui/material";
import {youtubeRegex} from "../utils/constants";
import markdownComponents from "../utils/markdownComponents";

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

    /**
     * Appends pasted images and video embeds to the field.
     *
     * <p>Takes the field's current text and hands onChange a plain string, the same as typing
     * does. Passing a setState-style updater instead only works for callers that happen to
     * forward it to setState; a caller that merges into an object - as the statement editor does
     * - stores the function itself and the field's text disappears.
     */
    const handlePaste = async (e, onChange, currentValue) => {
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
                        onChange((currentValue || "") + imageMarkdown);
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
                    onChange((currentValue || "") + embeddedVideoMarkdown);
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
                            onPaste={e => handlePaste(e, entry.onChange, entry.value)}
                        />
                    </>))
                }
                {activeTab === "editor" &&
                    <div style={{fontSize: "13px", color: "gray", marginBottom: "10px"}}>
                        {getMessage('ka', 'imageSizeHint')}
                    </div>
                }
                {activeTab === "preview" &&
                    <div style={{border: "1px solid #ccc", padding: "10px"}}>

                        {entries.map((entry) => (
                            <>
                                {entry.labelVisible &&
                                <p key={entry.label}
                                   style={{display: "block", marginBottom: "10px", fontWeight: "bold"}}>{entry.label}</p>
                                }
                                <div className="markdown-body">
                                    <ReactMarkdown
                                        children={entry.value}
                                        remarkPlugins={[remarkMath, remarkGfm]}
                                        rehypePlugins={[rehypeMathjax, rehypeRaw]}
                                        components={markdownComponents}
                                        urlTransform={imageDownloadFunc}
                                    />
                                </div>
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