
function setTimeout(callback, ms) {
    
    return NodeKitTimer.setTimeout(callback, ms)
}

function clearTimeout(indentifier) {
    
    NodeKitTimer.clearTimeout(indentifier)
}

function setInterval(callback, ms) {
    
    return NodeKitTimer.setInterval(callback, ms)
}