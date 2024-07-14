export interface NavigationItem {
    id: string;       // Unique identifier for the navigation item
    title: string;    // Display title of the navigation item
    url: string;      // URL the navigation item points to
    onlyMobile?: boolean;
}