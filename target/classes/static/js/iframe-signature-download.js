/**
 * Iframe Signature Download Utility
 * Handles PNG downloads from e-signature iframes
 */

class IframeSignatureDownloader {
    constructor() {
        this.pendingDownloads = new Map();
        this.setupMessageListener();
    }

    /**
     * Download signature from iframe
     * @param {string} iframeId - ID of the iframe containing the signature
     * @param {string} employeeId - Employee ID for filename
     */
    downloadFromIframe(iframeId, employeeId) {
        const iframe = document.getElementById(iframeId);
        
        if (!iframe) {
            console.error('Iframe not found:', iframeId);
            this.fallbackDownload(employeeId);
            return;
        }

        try {
            // Try direct iframe content download first
            iframe.contentWindow.postMessage({
                action: 'downloadSignature'
            }, '*');
            
            // Set timeout fallback
            setTimeout(() => {
                if (this.pendingDownloads.has(employeeId)) {
                    console.log('Direct download timeout, trying alternative method');
                    this.extractFromIframe(iframe, employeeId);
                }
            }, 5000);
            
        } catch (error) {
            console.error('Cross-origin iframe access denied:', error);
            this.extractFromIframe(iframe, employeeId);
        }
    }

    /**
     * Extract content from iframe and generate PNG
     */
    extractFromIframe(iframe, employeeId) {
        try {
            // Request signature data from iframe
            iframe.contentWindow.postMessage({
                action: 'getSignatureData'
            }, '*');
            
            // Store pending download request
            this.pendingDownloads.set(employeeId, {
                timestamp: Date.now(),
                iframe: iframe
            });
            
        } catch (error) {
            console.error('Cannot communicate with iframe:', error);
            this.fallbackDownload(employeeId);
        }
    }

    /**
     * Setup message listener for iframe communication
     */
    setupMessageListener() {
        window.addEventListener('message', (event) => {
            if (event.data.action === 'iframeReady') {
                console.log('Iframe ready:', event.data.employee);
                
            } else if (event.data.action === 'signatureDataResponse') {
                this.handleSignatureData(event.data.data);
                
            } else if (event.data.action === 'downloadComplete') {
                console.log('Download completed in iframe');
            }
        });
    }

    /**
     * Handle signature data received from iframe
     */
    handleSignatureData(signatureData) {
        if (!signatureData || !signatureData.employee) {
            console.error('Invalid signature data received');
            return;
        }

        const employeeId = signatureData.employee.empNo || 'unknown';
        const pendingDownload = this.pendingDownloads.get(employeeId);
        
        if (!pendingDownload) {
            console.log('No pending download for employee:', employeeId);
            return;
        }

        // Clear pending download
        this.pendingDownloads.delete(employeeId);

        // Create signature element in parent page
        this.createSignatureFromData(signatureData);
    }

    /**
     * Create signature element from iframe data and download as PNG
     */
    createSignatureFromData(signatureData) {
        // Create temporary container
        const tempContainer = document.createElement('div');
        tempContainer.style.position = 'absolute';
        tempContainer.style.left = '-9999px';
        tempContainer.style.top = '-9999px';
        tempContainer.innerHTML = signatureData.element;
        
        // Load CSS if needed
        this.loadSignatureCSS(signatureData.css).then(() => {
            document.body.appendChild(tempContainer);
            
            const signatureCard = tempContainer.querySelector('.id-card');
            if (signatureCard) {
                // Apply dimensions
                signatureCard.style.width = signatureData.dimensions.width + 'px';
                signatureCard.style.height = signatureData.dimensions.height + 'px';
                
                const filename = `${(signatureData.employee.name || 'signature').replace(/\s+/g, '_')}_${signatureData.employee.empNo || 'unknown'}_digital_signature.png`;
                
                // Use html2canvas to capture
                if (typeof html2canvas !== 'undefined') {
                    html2canvas(signatureCard, {
                        backgroundColor: '#ff4141',
                        scale: 3,
                        useCORS: true,
                        allowTaint: true,
                        foreignObjectRendering: true
                    }).then((canvas) => {
                        // Download the image
                        const link = document.createElement('a');
                        link.download = filename;
                        link.href = canvas.toDataURL('image/png', 1.0);
                        
                        document.body.appendChild(link);
                        link.click();
                        document.body.removeChild(link);
                        
                        // Clean up
                        document.body.removeChild(tempContainer);
                        
                        console.log('PNG download successful from extracted data');
                        
                    }).catch((error) => {
                        console.error('html2canvas error on extracted data:', error);
                        document.body.removeChild(tempContainer);
                        this.fallbackDownload(signatureData.employee.empNo);
                    });
                } else {
                    document.body.removeChild(tempContainer);
                    this.fallbackDownload(signatureData.employee.empNo);
                }
            } else {
                document.body.removeChild(tempContainer);
                this.fallbackDownload(signatureData.employee.empNo);
            }
        });
    }

    /**
     * Load signature CSS files
     */
    async loadSignatureCSS(cssUrls) {
        if (!cssUrls || cssUrls.length === 0) {
            return Promise.resolve();
        }

        const loadPromises = cssUrls.map(url => {
            return new Promise((resolve, reject) => {
                // Check if CSS is already loaded
                const existingLink = document.querySelector(`link[href="${url}"]`);
                if (existingLink) {
                    resolve();
                    return;
                }

                const link = document.createElement('link');
                link.rel = 'stylesheet';
                link.href = url;
                link.onload = () => resolve();
                link.onerror = () => resolve(); // Continue even if CSS fails to load
                
                document.head.appendChild(link);
            });
        });

        return Promise.all(loadPromises);
    }

    /**
     * Fallback download when iframe methods fail
     */
    fallbackDownload(employeeId) {
        console.log('Using fallback download method for employee:', employeeId);
        
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        
        canvas.width = 600;
        canvas.height = 300;
        
        // Background
        ctx.fillStyle = '#ff4141';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        
        // Text
        ctx.fillStyle = 'white';
        ctx.font = 'bold 24px Arial, sans-serif';
        ctx.textAlign = 'center';
        
        ctx.fillText('Digital Signature', canvas.width / 2, 80);
        ctx.font = '18px Arial, sans-serif';
        ctx.fillText('Employee ID: ' + (employeeId || 'Unknown'), canvas.width / 2, 140);
        ctx.fillText('SLTC Staff Digital Signature', canvas.width / 2, 200);
        
        // Download
        canvas.toBlob((blob) => {
            const link = document.createElement('a');
            link.download = `employee_${employeeId || 'unknown'}_signature.png`;
            link.href = URL.createObjectURL(blob);
            
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            URL.revokeObjectURL(link.href);
        }, 'image/png');
    }

    /**
     * Enhanced print function that captures iframe content
     */
    printFromIframe(iframeId) {
        const iframe = document.getElementById(iframeId);
        
        if (!iframe) {
            console.error('Iframe not found for printing:', iframeId);
            return;
        }

        try {
            // Focus on iframe and print
            iframe.contentWindow.focus();
            iframe.contentWindow.print();
        } catch (error) {
            console.error('Cannot access iframe for printing:', error);
            // Fallback: open iframe URL in new window and print
            window.open(iframe.src, '_blank');
        }
    }
}

// Global instance
window.iframeSignatureDownloader = new IframeSignatureDownloader();

// Utility functions for easy access
window.downloadSignatureFromIframe = function(iframeId, employeeId) {
    window.iframeSignatureDownloader.downloadFromIframe(iframeId, employeeId);
};

window.printSignatureFromIframe = function(iframeId) {
    window.iframeSignatureDownloader.printFromIframe(iframeId);
};