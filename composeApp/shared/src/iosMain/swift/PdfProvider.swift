import Foundation
import UIKit
import LinkPresentation

// This class is the key. It wraps our raw PDF data and provides metadata to the iOS share sheet.
@objc class PdfProvider: NSObject, UIActivityItemSource {
    
    private var pdfData: Data
    private var pdfTitle: String
    
    @objc init(pdfData: Data, pdfTitle: String) {
        self.pdfData = pdfData
        self.pdfTitle = pdfTitle
        super.init()
    }
    
    // This tells the share sheet what the placeholder item is.
    func activityViewControllerPlaceholderItem(_ activityViewController: UIActivityViewController) -> Any {
        return pdfData
    }
    
    // This provides the actual data to be shared.
    func activityViewController(_ activityViewController: UIActivityViewController, itemForActivityType activityType: UIActivity.ActivityType?) -> Any? {
        return pdfData
    }
    
    // This provides the metadata, like the title and the document type icon.
    func activityViewControllerLinkMetadata(_ activityViewController: UIActivityViewController) -> LPLinkMetadata? {
        let metadata = LPLinkMetadata()
        metadata.title = pdfTitle
        // The icon is determined by the file type, which is inferred from the data.
        // Giving it an icon manually helps ensure it's displayed correctly.
        metadata.iconProvider = NSItemProvider(object: UIImage(systemName: "doc.text.fill")!)
        return metadata
    }
}
