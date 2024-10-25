let peerConnection;
let localStream;
let remoteStream;

let stunServers = {
    iceServers: [
        {
            urls: [
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302",
            ]
        }
    ]
}


let init = async () => {
    localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: false
    });
    await createOffer();
}

let createOffer = async () => {
    peerConnection = new RTCPeerConnection(stunServers);
    remoteStream = new MediaStream();

    const audio = document.getElementById("audio");
    audio.srcObject = remoteStream;

    localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));

    peerConnection.ontrack = async event => {
        event.streams[0].getTracks().forEach(track => remoteStream.addTrack(track));
    };

    let offerJson = {}
    let canidates = 0;
    let sentOffer = false;

    peerConnection.onicecandidate = async event => {
        if (event.candidate) {
            document.getElementById("log").value += `Candidate:\n${JSON.stringify(event.candidate)}\n`;
            offerJson = peerConnection.localDescription;
            canidates++;
            console.log(`Candidates: ${canidates}`);
            if (canidates >= 2 && sentOffer === false) {
                console.log("Enough candidates - sending offer");
                sentOffer = true;
                await fetch("/offer", {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(peerConnection.localDescription)
                });
                document.getElementById("log").value += `FullOffer:\n${JSON.stringify(peerConnection.localDescription)}\n`;
            }
        }
    };

    let offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    offerJson = offer;

    document.getElementById("log").value += `Offer:\n${JSON.stringify(offer)}\n`;
}

init();