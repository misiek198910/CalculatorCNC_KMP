import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        return MainViewControllerKt.MainViewController { kotlinCallback in
            AppOpenAdHelper.shared.loadAndShow {
                DispatchQueue.main.async {

                    if let closure = kotlinCallback as? () -> Void {
                        closure()
                    } else if let closureWithUnit = kotlinCallback as? () -> KotlinUnit {
                        _ = closureWithUnit()
                    } else {
                        (kotlinCallback as AnyObject).invoke?()
                    }
                }
            }
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}